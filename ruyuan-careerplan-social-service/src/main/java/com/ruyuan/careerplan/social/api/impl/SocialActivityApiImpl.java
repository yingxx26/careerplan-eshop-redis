package com.ruyuan.careerplan.social.api.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.social.api.SocialActivityApi;
import com.ruyuan.careerplan.social.constants.SocialConstant;
import com.ruyuan.careerplan.social.constants.WordRoundConstant;
import com.ruyuan.careerplan.social.domain.dto.*;
import com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO;
import com.ruyuan.careerplan.social.domain.entity.SocialMasterDO;
import com.ruyuan.careerplan.social.enums.DelFlagEnum;
import com.ruyuan.careerplan.social.enums.ActivityStatusEnum;
import com.ruyuan.careerplan.social.enums.StageStatusEnum;
import com.ruyuan.careerplan.social.service.*;
import com.ruyuan.careerplan.social.utils.SocialUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class SocialActivityApiImpl implements SocialActivityApi {

    @Resource
    private RedisCache redisCache;

    @Resource
    private RedisLock redisLock;

    @Resource
    private SocialMasterService socialMasterService;

    @Resource
    private SocialInviteeService socialInviteeService;

    @Resource
    private SocialActivityConfigService socialActivityConfigService;

    @Resource
    private SocialActivityService socialActivityService;

    @Resource
    private SocialCommonService socialCommonService;

    @Value("${social.wechat.url}")
    private String wechatMiniUrl;

    @Value("${social.wechat.organization.code}")
    private String organizationCode;

    /**
     * 总共所需分享群聊数
     * 注：文本中的第n次取此参数
     */
    private static final int TOTAL_NEED_SHARE_COUNT = 2;

    /**
     * 分享奖励规则限制
     */
    private static final int SHARE_COUNT = 3;

    /**
     * 数字值
     */
    private static final int INTEGER_1 = 1;

    /**
     * 字符值
     */
    private static final String me = "我";

    /**
     * 菜谱分享活动
     *
     * @param userId
     * @param cookbookId
     * @return com.ruyuan.careerplan.common.core.JsonResult<java.util.Map < java.lang.String, java.lang.Object>>
     * @author zhonghuashishan
     */
    @Override
    public JsonResult<Map<String, Object>> cookbookShare(Long userId, String cookbookId) {
        // 参数校验
        if (Objects.isNull(userId) || StringUtils.isEmpty(cookbookId)) {
            return JsonResult.buildError("userId或cookbookId不能为空");
        }

        Map<String, Object> map = new HashMap<>();
        Long alreadyShareCount = redisCache.increment(SocialConstant.COOKBOOK_SHARE_COUNT_KEY + cookbookId, INTEGER_1);
        if (alreadyShareCount > SHARE_COUNT) {
            // 分享次数大于n次，不走后面奖励逻辑
            map.put("alreadyShareCount", alreadyShareCount);
            return JsonResult.buildSuccess(map);
        }

        // 获取团长表缓存
        SocialMasterDO socialMasterDO = getSocialMaster(cookbookId);
        if (Objects.isNull(socialMasterDO)) {
            return JsonResult.buildSuccess(map);
        }

        if (!userId.equals(socialMasterDO.getCreatorId())) {
            log.warn("请求错误，你不是团长，只有团长才能领取后面的奖励");
            return JsonResult.buildError("请求错误，你不是团长");
        }

        SocialActivityConfigDTO socialActivityConfigDTO = getSocialActivityConfig();
        if (ActivityStatusEnum.WAITING.getCode() == socialMasterDO.getHelpStatus()) {

            // 首次分享即可开启活动
            runActivity(socialMasterDO, socialActivityConfigDTO);

            WeChatShareDataDTO weChatShareDataDTO = WeChatShareDataDTO
                    .builder()
                    .organizationCode(organizationCode)
                    .miniTitle(socialActivityConfigDTO.getShareMsg())
                    .miniDesc(socialActivityConfigDTO.getShareMsg())
                    .miniUrl(String.format(wechatMiniUrl, socialMasterDO.getCookbookId()))
                    .imageUrl(socialActivityConfigDTO.getShareImg())
                    .build();
            map.put("weChatMiniAppShareInfo", weChatShareDataDTO);
        }

        if (socialMasterDO.getHelpStatus() != ActivityStatusEnum.UNDERWAY.getCode()) {
            // 活动异常情况
            return JsonResult.buildError("活动已结束");
        }

        String lockKey = SocialConstant.JOIN_HELP_CONCURRENCE_LOCK_KEY + cookbookId;
        try {
            if (!redisLock.lock(lockKey)) {
                return JsonResult.buildError("活动火爆，请稍后再试");
            }

            SocialMasterExtendDTO socialMasterExtendDTO = socialMasterDO.getSocialMasterExtendDTO();
            socialMasterExtendDTO.setAlreadyShareCount(alreadyShareCount);
            socialMasterExtendDTO.setShareTwoAmount(socialActivityConfigDTO.getShareTwoAmount());

            map.put("alreadyShareCount", alreadyShareCount);
            map.put("shareAmount", socialActivityConfigDTO.getShareTwoAmount());
            map.put("remainAmount", socialMasterExtendDTO.getRemainAmount());

            if (alreadyShareCount == TOTAL_NEED_SHARE_COUNT && !socialMasterExtendDTO.getShareTwoPayingAmount()) {
                socialMasterExtendDTO.setShareTwoPayingAmount(true);
                // 分享群聊或好友次数大于n，立即提现的金额算到额外奖励上
                setCommonSocialAmount(userId, cookbookId, socialMasterDO, socialMasterExtendDTO, socialActivityConfigDTO.getShareTwoAmount(), true);
                createInvite(userId, socialActivityConfigDTO.getShareTwoAmount(), socialMasterDO);
                map.put("remainAmount", socialMasterExtendDTO.getRemainAmount());
            }

            updateSocialMaster(socialMasterDO, socialMasterExtendDTO);
            return JsonResult.buildSuccess(map);
        } finally {
            redisLock.unlock(lockKey);
        }
    }

    /**
     * 进入社交活动
     *
     * @param userId
     * @param cookbookId
     * @param ip
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.social.domain.dto.SocialMasterDetailResultDTO>
     * @author zhonghuashishan
     */
    @Override
    public JsonResult<SocialMasterDetailResultDTO> enterSocialActivity(Long userId, String cookbookId, String ip) {
        // 参数校验
        if (Objects.isNull(userId) || StringUtils.isEmpty(cookbookId) || StringUtils.isEmpty(ip)) {
            return JsonResult.buildError("userId或cookbookId不能为空");
        }

        // 取团长信息
        SocialMasterDO socialMasterDO = getSocialMaster(cookbookId);
        if (Objects.isNull(socialMasterDO)) {
            return JsonResult.buildError("活动不存在");
        }

        SocialMasterExtendDTO socialMasterExtendDTO = socialMasterDO.getSocialMasterExtendDTO();
        SocialMasterDetailResultDTO socialMasterDetailResultDTO = new SocialMasterDetailResultDTO();
        socialMasterDetailResultDTO.setCookbookId(cookbookId);
        socialMasterDetailResultDTO.setMasterAvatar(socialMasterDO.getMasterAvatar());
        socialMasterDetailResultDTO.setMasterNickname(socialMasterDO.getMasterNickname());
        socialMasterDetailResultDTO.setOneself(userId.equals(socialMasterDO.getCreatorId()));
        socialMasterDetailResultDTO.setHelpStatus(socialMasterDO.getHelpStatus());
        socialMasterDetailResultDTO.setShowPremiums(true);

        if (Objects.isNull(socialMasterExtendDTO)) {
            socialMasterDetailResultDTO.setHelpStatus(ActivityStatusEnum.EXPIRED.getCode());
            return JsonResult.buildSuccess(socialMasterDetailResultDTO);
        }

        boolean isNewUser = socialCommonService.isNewUser(userId);
        socialMasterDetailResultDTO.setNewUserFlag(isNewUser);

        // 发起者，群分享阶段更新内容
        if (userId.equals(socialMasterDO.getCreatorId())) {
            handlerMaster(userId, cookbookId, socialMasterDO, socialMasterExtendDTO, socialMasterDetailResultDTO);
            return JsonResult.buildSuccess(socialMasterDetailResultDTO);
        }

        // 助力者，更新内容
        SocialActivityHelpResultDTO socialActivityHelpResultDTO = enterSocialInviteeActivity(userId, cookbookId, ip);

        // 金额变动的情况，需要重新获取下缓存
        socialMasterDO = getSocialMaster(cookbookId);
        socialMasterExtendDTO = socialMasterDO.getSocialMasterExtendDTO();
        socialMasterDetailResultDTO.setHelpActivityResult(socialActivityHelpResultDTO);
        getMasterDetailResult(userId, cookbookId, socialMasterDO, socialMasterExtendDTO, socialMasterDetailResultDTO);

        return JsonResult.buildSuccess(socialMasterDetailResultDTO);
    }

    /**
     * 发起者——团长分享主页详情
     *
     * @param userId
     * @param cookbookId
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param socialMasterDetailResultDTO
     * @return void
     * @author zhonghuashishan
     */
    private void handlerMaster(Long userId, String cookbookId, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, SocialMasterDetailResultDTO socialMasterDetailResultDTO) {
        SocialActivityConfigDTO socialActivityConfigDTO = getSocialActivityConfig();
        if (ActivityStatusEnum.WAITING.getCode() == socialMasterDO.getHelpStatus()) {
            // 首次分享即可开启活动
            runActivity(socialMasterDO, socialActivityConfigDTO);
        }

        // 社交活动配置
        socialMasterDetailResultDTO.setRuleDocuments(socialActivityConfigDTO.getRuleDocuments());
        List<SocialInviteeDO> socialInviteeList = getSocialInviteeList(cookbookId);
        socialMasterDetailResultDTO.setHelpStatus(socialMasterDO.getHelpStatus());
        socialMasterDetailResultDTO.setAlreadyShareCount(socialMasterExtendDTO.getAlreadyShareCount());
        socialMasterDetailResultDTO.setShareAmount(socialActivityConfigDTO.getShareTwoAmount());

        if (socialMasterDO.getHelpStatus() == ActivityStatusEnum.UNDERWAY.getCode()) {
            Long expire = redisCache.getExpire(SocialConstant.WITHDRAWAL_COUNTDOWN_KEY + cookbookId, TimeUnit.MILLISECONDS);
            socialMasterDetailResultDTO.setRemainTime(expire < 0 ? 0 : expire);

            // 分享群聊或好友次数大于2，立即提现的金额算到额外奖励上
            String alreadyShareTimesStr = redisCache.get(SocialConstant.COOKBOOK_SHARE_COUNT_KEY + cookbookId);
            int alreadyShareCount = NumberUtils.toInt(alreadyShareTimesStr, 0);

            // 群分享阶段
            if (socialMasterExtendDTO.getAlreadyShareCount() <= TOTAL_NEED_SHARE_COUNT && !socialMasterExtendDTO.getShareTwoPayingAmount()) {
                socialMasterDetailResultDTO.setTotalAmount(socialMasterDO.getTotalAmount());
                socialMasterDetailResultDTO.setWaitAmount(socialMasterExtendDTO.getWaitAmount());
                socialMasterDetailResultDTO.setReceiveAmount(socialMasterExtendDTO.getReceiveAmount());
                socialMasterDetailResultDTO.setReadyAmount(socialMasterExtendDTO.getWithdrawAmount());
                socialMasterDetailResultDTO.setRemainAmount(socialMasterExtendDTO.getRemainAmount());

                // 处于分享群聊/好友阶段
                socialMasterDetailResultDTO.setTotalNeedShareCount(TOTAL_NEED_SHARE_COUNT);
                socialMasterDetailResultDTO.setNeedInviteOrShareCount(TOTAL_NEED_SHARE_COUNT - alreadyShareCount);

                // 所处阶段——群分享
                socialMasterDetailResultDTO.setCurrentStage(StageStatusEnum.SHARE_GROUP.getCode());
                socialMasterDetailResultDTO.setReadyAmount(socialMasterDO.getTotalAmount() - socialMasterExtendDTO.getRemainAmount());
                setInviteeList(userId, socialMasterDO, socialMasterExtendDTO, socialActivityConfigDTO, socialInviteeList, socialMasterDetailResultDTO);
                return;
            }

            // 当前阶段默认不需要弹红包
            socialMasterDetailResultDTO.setCurrentStage(StageStatusEnum.BUITY_ING.getCode());
            String currentStageStr = redisCache.get(SocialConstant.CURRENT_STAGE_KEY + cookbookId);
            int currentStage = NumberUtils.toInt(currentStageStr, INTEGER_1);
            List<Integer> stageMemberList = getStageMembers(cookbookId);

            // 当前阶段还需邀请人数
            socialMasterDetailResultDTO.setNeedInviteOrShareCount(SocialUtil.stageMembers(currentStage, socialMasterExtendDTO, stageMemberList) - socialMasterExtendDTO.getHelpCount());

            // 满足阶段助力设置的缓存，本次弹完额外奖励后删除
            String premiumsAmount = redisCache.get(SocialConstant.CURRENT_STAGE_PREMIUMS_KEY + cookbookId);
            if (StringUtils.isNotEmpty(premiumsAmount)) {
                // 当前阶段需要弹红包
                socialMasterDetailResultDTO.setCurrentStage(StageStatusEnum.RED_PACK.getCode());
                socialMasterDetailResultDTO.setPremiumsAmount(Integer.valueOf(premiumsAmount));
                redisCache.delete(SocialConstant.CURRENT_STAGE_PREMIUMS_KEY + cookbookId);
            }

            // 判断当前是否是最后一个阶段，是则不展示额外奖励
            if (nowStageIsLast(cookbookId)) {
                socialMasterDetailResultDTO.setShowPremiums(false);
                socialMasterDetailResultDTO.setCurrentStage(StageStatusEnum.BUITY_NOT_TRIGGER.getCode());
            }
        }

        socialMasterDetailResultDTO.setTotalAmount(socialMasterDO.getTotalAmount());
        socialMasterDetailResultDTO.setWaitAmount(socialMasterExtendDTO.getWaitAmount());
        socialMasterDetailResultDTO.setReceiveAmount(socialMasterExtendDTO.getReceiveAmount());
        socialMasterDetailResultDTO.setReadyAmount(socialMasterExtendDTO.getWithdrawAmount());
        socialMasterDetailResultDTO.setRemainAmount(socialMasterExtendDTO.getRemainAmount());
        socialMasterDO.setSocialMasterExtendDTO(socialMasterExtendDTO);
        setInviteeList(userId, socialMasterDO, socialMasterExtendDTO, socialActivityConfigDTO, socialInviteeList, socialMasterDetailResultDTO);
    }

    /**
     * 助力阶段数以及对应需要邀请人数
     *
     * @param cookbookId
     * @return java.util.List<java.lang.Integer>
     * @author zhonghuashishan
     */
    private List<Integer> getStageMembers(String cookbookId) {
        String stageMemberStr = redisCache.get(SocialConstant.HELP_STAGE_MEMBER_KEY + cookbookId);
        return JSONArray.parseArray(stageMemberStr, Integer.class);
    }

    /**
     * 判断当前是否是最后一个阶段
     *
     * @param cookbookId
     * @return boolean
     * @author zhonghuashishan
     */
    private boolean nowStageIsLast(String cookbookId) {
        String currentStageStr = redisCache.get(SocialConstant.CURRENT_STAGE_KEY + cookbookId);
        int currentStage = NumberUtils.toInt(currentStageStr, INTEGER_1);
        List<Integer> stageMemberList = getStageMembers(cookbookId);
        return currentStage == stageMemberList.size();
    }

    /**
     * 第2次分享参团信息
     *
     * @param userId
     * @param helpAmount
     * @param socialMasterDO
     * @return void
     * @author zhonghuashishan
     */
    private void createInvite(Long userId, int helpAmount, SocialMasterDO socialMasterDO) {
        try {
            SocialInviteeDO socialInviteeDO = new SocialInviteeDO();
            SocialInviteeExtendDTO socialInviteeExtendDTO = new SocialInviteeExtendDTO();
            String roundWord = WordRoundConstant.getRoundWord(helpAmount);
            socialInviteeExtendDTO.setHelpAmountDoc(roundWord);
            socialInviteeExtendDTO.setUserId(userId);
            socialInviteeExtendDTO.setPremiums(false);
            socialInviteeExtendDTO.setNewUserFlag(false);
            socialInviteeDO.setCookbookId(socialMasterDO.getCookbookId());
            socialInviteeDO.setInviteeId(userId);
            socialInviteeDO.setInviteeNickName(socialMasterDO.getMasterNickname());
            socialInviteeDO.setInviteeAvatar(socialMasterDO.getMasterAvatar());
            socialInviteeDO.setHelpAmount(helpAmount);
            socialInviteeDO.setCreateTime(new Date());
            socialInviteeDO.setUpdateTime(new Date());
            socialInviteeDO.setHelpConfig(JSON.toJSONString(socialInviteeExtendDTO));
            socialInviteeDO.setDelFlag(DelFlagEnum.EFFECTIVE.getCode());
            socialInviteeService.insertSocialInvitee(socialInviteeDO);
        } catch (Exception e) {
            log.error("异常 入参为cookbookId={},userId={},socialMasterDO={},error=", socialMasterDO.getCookbookId(), userId, JSON.toJSONString(socialMasterDO), e);
        }
    }

    /**
     * 分享阶段设置金额
     *
     * @param userId
     * @param cookbookId
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param premiumsAmount
     * @param stageFlag
     * @return void
     * @author zhonghuashishan
     */
    private void setCommonSocialAmount(Long userId, String cookbookId, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, int premiumsAmount, boolean stageFlag) {
        // 待到账金额，原有的金额+本次助力金额+额外奖励金额（注：满足阶段后清空）
        socialMasterExtendDTO.setWaitAmount(socialMasterExtendDTO.getWaitAmount());

        // 满足分享阶段或第n次分享
        if (stageFlag && premiumsAmount > 0) {
            JSONObject json = new JSONObject();
            json.put("cookbookId", cookbookId);
            json.put("userId", userId);
            json.put("remainAmount", socialMasterExtendDTO.getRemainAmount());
            json.put("premiumsAmount", premiumsAmount);
            socialCommonService.sendMessage(SocialConstant.PAYING_AMOUNT_TOPIC, json.toJSONString());

            // 已到账金额，第n次群分享+满足阶段提现（判断是否满足，满足则清空待到账金额）
            socialMasterExtendDTO.setReceiveAmount(socialMasterExtendDTO.getReceiveAmount() + premiumsAmount);
        }

        // 已提的金额（微信充值成功 第n次群分享+满足阶段提现）+待到账金额
        socialMasterExtendDTO.setWithdrawAmount(socialMasterExtendDTO.getReceiveAmount() + socialMasterExtendDTO.getWaitAmount());
        // 待提现金额，总金额-已提现金额
        socialMasterExtendDTO.setRemainAmount(socialMasterDO.getTotalAmount() - socialMasterExtendDTO.getWithdrawAmount());
        socialMasterDO.setSocialMasterExtendDTO(socialMasterExtendDTO);
    }

    /**
     * 更新团长表
     *
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @return void
     * @author zhonghuashishan
     */
    private void updateSocialMaster(SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO) {
        socialMasterDO.setMasterConfig(JSON.toJSONString(socialMasterExtendDTO));
        socialMasterService.updateSocialMasterByIdSelective(socialMasterDO);
        redisCache.setex(SocialConstant.SOCIAL_MASTER_INFO_KEY + socialMasterDO.getCookbookId(), JSON.toJSONString(socialMasterDO), INTEGER_1, TimeUnit.DAYS);
    }

    /**
     * 活动倒计时
     *
     * @param socialMasterDO
     * @return void
     * @author zhonghuashishan
     */
    private void runActivity(SocialMasterDO socialMasterDO, SocialActivityConfigDTO socialActivityConfigDTO) {
        long activityStartCount = redisCache.increment(SocialConstant.ACTIVITY_START_COUNT_KEY + socialMasterDO.getCookbookId(), INTEGER_1);
        // 首次请求才发送消息
        if (activityStartCount != INTEGER_1) {
            return;
        }

        Date now = new Date();
        socialMasterDO.setStartTime(now);
        Date endTime = getEndTime(now, socialActivityConfigDTO);
        socialMasterDO.setEndTime(endTime);

        socialMasterDO.setHelpStatus(ActivityStatusEnum.UNDERWAY.getCode());
        boolean success = updateMaster(socialMasterDO);
        if (success) {
            redisCache.delete(SocialConstant.SOCIAL_MASTER_INFO_KEY + socialMasterDO.getCookbookId());
            redisCache.setex(SocialConstant.WITHDRAWAL_COUNTDOWN_KEY + socialMasterDO.getCookbookId(), String.valueOf(INTEGER_1), INTEGER_1, TimeUnit.DAYS);
        }

        redisCache.expireAt(SocialConstant.ACTIVITY_START_COUNT_KEY + socialMasterDO.getCookbookId(), SocialUtil.getNextDay(INTEGER_1));
    }

    /**
     * 更新团长信息
     *
     * @param socialMasterDO
     * @return boolean
     * @author zhonghuashishan
     */
    private boolean updateMaster(SocialMasterDO socialMasterDO) {
        int i = socialMasterService.updateSocialMasterByIdSelective(socialMasterDO);
        if (i > 0) {
            return true;
        }
        return false;
    }

    /**
     * 进入助力活动
     *
     * @param userId
     * @param cookbookId
     * @param ip
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO enterSocialInviteeActivity(Long userId, String cookbookId, String ip) {
        return socialActivityService.enterSocialInviteeActivity(cookbookId, userId, ip);
    }

    /**
     * 好友助力详情结果
     *
     * @param userId
     * @param cookbookId
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param socialMasterDetailResultDTO
     * @return void
     * @author zhonghuashishan
     */
    private void getMasterDetailResult(Long userId, String cookbookId, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, SocialMasterDetailResultDTO socialMasterDetailResultDTO) {
        try {
            // 社交活动配置
            SocialActivityConfigDTO socialActivityConfig = getSocialActivityConfig();
            socialMasterDetailResultDTO.setRuleDocuments(socialActivityConfig.getRuleDocuments());
            setCommonSocialAmount(userId, cookbookId, socialMasterDO, socialMasterExtendDTO, 0, false);

            socialMasterDetailResultDTO.setTotalAmount(socialMasterDO.getTotalAmount());
            socialMasterDetailResultDTO.setWaitAmount(socialMasterExtendDTO.getWaitAmount());
            socialMasterDetailResultDTO.setReceiveAmount(socialMasterExtendDTO.getReceiveAmount());
            socialMasterDetailResultDTO.setReadyAmount(socialMasterExtendDTO.getWithdrawAmount());
            socialMasterDetailResultDTO.setRemainAmount(socialMasterExtendDTO.getRemainAmount());

            List<SocialInviteeDO> socialInviteeList = getSocialInviteeList(cookbookId);
            setInviteeList(userId, socialMasterDO, socialMasterExtendDTO, socialActivityConfig, socialInviteeList, socialMasterDetailResultDTO);
        } catch (Exception e) {
            log.error("好友助力列表异常 入参为userId={},cookbookId={},error=", userId, cookbookId, e);
        }
    }

    /**
     * 设置助力人列表信息
     *
     * @param userId
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param socialActivityConfig
     * @param socialInviteeDOList
     * @param socialMasterDetailResultDTO
     * @return void
     * @author zhonghuashishan
     */
    private void setInviteeList(Long userId, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, SocialActivityConfigDTO socialActivityConfig, List<SocialInviteeDO> socialInviteeDOList, SocialMasterDetailResultDTO socialMasterDetailResultDTO) {
        try {
            if (Objects.isNull(socialInviteeDOList)) {
                return;
            }

            int totalPremiumsAmount = 0;
            List<SocialInviteeCollectDTO> socialInviteeCollectDTOList = Lists.newArrayList();
            List<SocialInviteeDO> inviteeList = socialInviteeDOList.stream().sorted(Comparator.comparing(SocialInviteeDO::getId).reversed()).collect(Collectors.toList());

            for (SocialInviteeDO socialInviteeDO : inviteeList) {
                SocialInviteeCollectDTO socialInviteeCollectDTO = new SocialInviteeCollectDTO();
                socialInviteeCollectDTO.setInviteeId(socialInviteeDO.getInviteeId());
                socialInviteeCollectDTO.setHelpAmount(socialInviteeDO.getHelpAmount());
                socialInviteeCollectDTO.setInviteeNickName(socialInviteeDO.getInviteeNickName());
                socialInviteeCollectDTO.setInviteeAvatar(socialInviteeDO.getInviteeAvatar());
                SocialInviteeExtendDTO socialInviteeExtendDTO = socialInviteeDO.getSocialInviteeExtendDTO();
                socialInviteeCollectDTO.setUserId(socialInviteeExtendDTO.getUserId());
                socialInviteeCollectDTO.setHelpAmountDoc(socialInviteeExtendDTO.getHelpAmountDoc());
                socialInviteeCollectDTO.setNewUserFlag(socialInviteeExtendDTO.getNewUserFlag());
                socialInviteeCollectDTO.setPremiums(socialInviteeExtendDTO.getPremiums());

                if (socialInviteeDO.getInviteeId().equals(userId)) {
                    socialInviteeCollectDTO.setInviteeNickName(me);
                    socialInviteeCollectDTO.setOneself(true);
                } else if (socialInviteeDO.getInviteeId().intValue() == 0) {
                    socialInviteeCollectDTO.setPremiums(true);
                    socialInviteeCollectDTO.setOneself(false);
                    totalPremiumsAmount += socialInviteeDO.getHelpAmount();
                }

                socialInviteeCollectDTOList.add(socialInviteeCollectDTO);
            }
            socialMasterDetailResultDTO.setInviteeList(socialInviteeCollectDTOList);

            if (socialMasterDO.getHelpStatus() == ActivityStatusEnum.FINISH.getCode() || socialMasterDO.getHelpStatus() == ActivityStatusEnum.EXPIRED.getCode()) {
                socialMasterDetailResultDTO.setTotalInviteeCount(socialMasterExtendDTO.getHelpCount());
                socialMasterDetailResultDTO.setTotalPremiumsAmount(totalPremiumsAmount);
            }
        } catch (Exception e) {
            log.error("设置助力人列表信息异常 入参为cookbookId={},userId={},socialInviteeDOList={},error=", socialMasterDO.getCookbookId(), userId, JSON.toJSONString(socialInviteeDOList), e);
        }
    }

    /**
     * 获取团长信息缓存
     *
     * @param cookbookId
     * @return com.ruyuan.careerplan.social.domain.dto.SocialMasterDTO
     * @author zhonghuashishan
     */
    private SocialMasterDO getSocialMaster(String cookbookId) {
        String socialMasterStr = redisCache.get(SocialConstant.SOCIAL_MASTER_INFO_KEY + cookbookId);
        if (StringUtils.isNotEmpty(socialMasterStr)) {
            JSONObject jsonObject = JSON.parseObject(socialMasterStr);
            return JSON.toJavaObject(jsonObject, SocialMasterDO.class);
        }

        // 对于你来说，如果你发布 了菜谱，菜谱里可以种草一些商品，对这些商品发起分享助力的活动
        // 你就是这个菜谱的活动团长，活动团长，他就可以包含一系列的信息
        SocialMasterDO socialMasterDO = socialMasterService.selectSocialMasterByCookbookId(cookbookId);
        if (Objects.nonNull(socialMasterDO)) {
//            socialMasterDO.setSocialMasterExtendDTO(JSON.parseObject(socialMasterDO.getMasterConfig(), SocialMasterExtendDTO.class));
            redisCache.setex(SocialConstant.SOCIAL_MASTER_INFO_KEY + cookbookId, JSON.toJSONString(socialMasterDO), INTEGER_1, TimeUnit.DAYS);
        }

        return socialMasterDO;
    }

    /**
     * 获取参团信息
     *
     * @param cookbookId
     * @return
     */
    private List<SocialInviteeDO> getSocialInviteeList(String cookbookId) {
        SocialInviteeDO socialInviteeDO = new SocialInviteeDO();
        socialInviteeDO.setCookbookId(cookbookId);
        return socialInviteeService.selectSocialInviteeList(socialInviteeDO);
    }

    /**
     * 计算结束时间
     *
     * @param startTime
     * @param socialActivityConfigDTO
     * @return java.util.Date
     * @author zhonghuashishan
     */
    private Date getEndTime(Date startTime, SocialActivityConfigDTO socialActivityConfigDTO) {
        int sharingValidTime = socialActivityConfigDTO.getCountdownTime();
        return DateUtils.addHours(startTime, sharingValidTime);
    }

    /**
     * 获取社交活动配置
     *
     * @param
     * @return com.ruyuan.careerplan.social.domain.dto.SocialActivityConfigDTO
     * @author zhonghuashishan
     */
    private SocialActivityConfigDTO getSocialActivityConfig() {
        return socialActivityConfigService.getSocialActivityConfig();
    }


}


