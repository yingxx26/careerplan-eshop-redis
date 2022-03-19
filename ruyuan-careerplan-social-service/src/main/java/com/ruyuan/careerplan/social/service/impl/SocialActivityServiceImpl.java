package com.ruyuan.careerplan.social.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.social.constants.SocialConstant;
import com.ruyuan.careerplan.social.constants.WordRoundConstant;
import com.ruyuan.careerplan.social.domain.dto.*;
import com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO;
import com.ruyuan.careerplan.social.domain.entity.SocialMasterDO;
import com.ruyuan.careerplan.social.enums.CouponStatusEnum;
import com.ruyuan.careerplan.social.enums.DelFlagEnum;
import com.ruyuan.careerplan.social.enums.HelpCopywriterEnum;
import com.ruyuan.careerplan.social.enums.ActivityStatusEnum;
import com.ruyuan.careerplan.social.service.*;
import com.ruyuan.careerplan.social.utils.SocialUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SocialActivityServiceImpl implements SocialActivityService {

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
    private SocialCommonService socialCommonService;

    /**
     * 随机
     */
    private static final Random random = new Random();

    /**
     * 过期时间
     */
    private static final int DAYS_1 = 1;
    private static final int DAYS_2 = 2;

    /**
     * 数字值
     */
    private static final int INTEGER_1 = 1;
    private static final int INTEGER_5 = 5;
    private static final int INTEGER_10 = 10;
    private static final int INTEGER_100 = 100;
    private static final int INTEGER_300 = 300;

    /**
     * 进入助力活动
     *
     * @param cookbookId
     * @param userId
     * @param ip
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    @Override
    public SocialActivityHelpResultDTO enterSocialInviteeActivity(String cookbookId, Long userId, String ip) {
        // 参数校验
        if (Objects.isNull(userId) || StringUtils.isEmpty(cookbookId)) {
            return null;
        }

        SocialActivityHelpResultDTO socialActivityHelpResultDTO = new SocialActivityHelpResultDTO();
        SocialMasterDO socialMasterDO = getSocialMaster(cookbookId);
        boolean isNewUser = socialCommonService.isNewUser(userId);

        // 活动结束，不可助力
        boolean finish = isFinish(cookbookId);
        if (finish) {
            SocialMasterExtendDTO socialMasterExtendDTO = socialMasterDO.getSocialMasterExtendDTO();
            String readyAmount = BigDecimal.valueOf((long) socialMasterExtendDTO.getWithdrawAmount()).divide(new BigDecimal(100)).toString();

            int helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE_FINISH.getCode();
            String helpCopywriterDesc = String.format(HelpCopywriterEnum.HELP_FAILURE_FINISH.getDesc(), readyAmount);
            if (isNewUser) {
                //新用户
                helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_SUCCESS_AGAIN.getCode();
                helpCopywriterDesc = String.format(HelpCopywriterEnum.NEW_USER_HELP_SUCCESS_AGAIN.getDesc(), readyAmount);
            }

            socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
            socialActivityHelpResultDTO.setHelpAmount(0);
            return socialActivityHelpResultDTO;
        }

        // 重复助力
        String joinHelpFlag = redisCache.get(SocialConstant.JOIN_HELP_FLAG_KEY + cookbookId + "_" + userId);
        if (StringUtils.isNotEmpty(joinHelpFlag)) {
            SocialInviteeDO socialInvitee = socialInviteeService.selectSocialInviteeByCondition(cookbookId, userId);
            String helpAmount = BigDecimal.valueOf(Long.valueOf(socialInvitee.getHelpAmount())).divide(new BigDecimal(100)).toString();

            int helpCopywriterCode = HelpCopywriterEnum.HELP_SUCCESS_AGAIN.getCode();
            String helpCopywriterDesc = String.format(HelpCopywriterEnum.HELP_SUCCESS_AGAIN.getDesc(), helpAmount);
            if (isNewUser) {
                //新用户
                helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_SUCCESS_AGAIN.getCode();
                helpCopywriterDesc = String.format(HelpCopywriterEnum.NEW_USER_HELP_SUCCESS_AGAIN.getDesc(), helpAmount);
            }

            socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
            socialActivityHelpResultDTO.setHelpAmount(socialInvitee.getHelpAmount());
            return socialActivityHelpResultDTO;
        }

        // 被风控，不可助力
        Boolean isRiskUser = socialCommonService.isRiskUser(userId, ip);
        if (isRiskUser) {
            int helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE_RISK.getCode();
            String helpCopywriterDesc = HelpCopywriterEnum.HELP_FAILURE_RISK.getDesc();
            if (isNewUser) {
                // 新用户
                helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_FAILURE_RISK.getCode();
                helpCopywriterDesc = HelpCopywriterEnum.NEW_USER_HELP_FAILURE_RISK.getDesc();
            }

            socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
            socialActivityHelpResultDTO.setHelpAmount(0);

            return socialActivityHelpResultDTO;
        }

        // X天内不可给同一好友助力
        String threeDayLimit = redisCache.get(SocialConstant.HELP_INTERVAL_LIMIT_KEY + socialMasterDO.getCreatorId() + "_" + userId);
        if (StringUtils.isNotEmpty(threeDayLimit)) {
            int helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE_SAME_LIMIT.getCode();
            String helpCopywriterDesc = String.format(HelpCopywriterEnum.HELP_FAILURE_SAME_LIMIT.getDesc(), threeDayLimit);
            if (isNewUser) {
                helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_FAILURE_SAME_LIMIT.getCode();
                helpCopywriterDesc = String.format(HelpCopywriterEnum.NEW_USER_HELP_FAILURE_SAME_LIMIT.getDesc(), threeDayLimit);
            }

            socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
            socialActivityHelpResultDTO.setHelpAmount(0);

            return socialActivityHelpResultDTO;
        }

        // 助力已达上限，不可助力
        Long helpCountLimit = redisCache.increment(SocialConstant.SUCCESS_HELP_COUNT_KEY + !isNewUser + "_" + userId, INTEGER_1);
        if (!isNewUser && helpCountLimit == INTEGER_1) {
            // 老用户的redis设置当前过期时间
            redisCache.expireAt(SocialConstant.SUCCESS_HELP_COUNT_KEY + !isNewUser + userId, SocialUtil.getCurrentEndDate());
        }

        int canHelpCount = helpCountLimit(isNewUser, cookbookId);
        if (canHelpCount < helpCountLimit) {
            // 此次助力不成功，还原助力次数
            redisCache.increment(SocialConstant.SUCCESS_HELP_COUNT_KEY + !isNewUser + "_" + userId, -INTEGER_1);

            int helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE_COUNT_LIMIT.getCode();
            String helpCopywriterDesc = HelpCopywriterEnum.HELP_FAILURE_COUNT_LIMIT.getDesc();
            if (isNewUser) {
                helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_FAILURE_COUNT_LIMIT.getCode();
                helpCopywriterDesc = HelpCopywriterEnum.NEW_USER_HELP_FAILURE_COUNT_LIMIT.getDesc();
            }

            socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
            socialActivityHelpResultDTO.setHelpAmount(0);

            return socialActivityHelpResultDTO;
        }

        // 获取助力人自己操作的锁，防止多次助力
        String sameInviteeKey = SocialConstant.JOIN_HELP_SAME_LOCK_KEY + cookbookId + "_" + userId;
        boolean lock = redisLock.lock(sameInviteeKey, INTEGER_10);
        if (!lock) {
            // 此次助力不成功，还原助力次数
            redisCache.increment(SocialConstant.SUCCESS_HELP_COUNT_KEY + !isNewUser + "_" + userId, -INTEGER_1);

            socialActivityHelpResultDTO.setHelpCopywriterCode(HelpCopywriterEnum.HELP_FAILURE.getCode());
            socialActivityHelpResultDTO.setHelpCopywriterDesc(HelpCopywriterEnum.HELP_FAILURE.getDesc());
            return socialActivityHelpResultDTO;
        }

        // 可助力
        return getInviteeHelpResult(socialActivityHelpResultDTO, isNewUser, cookbookId, userId);
    }

    /**
     * 获取可助力结果
     *
     * @param socialActivityHelpResultDTO
     * @param isNewUser
     * @param cookbookId
     * @param userId
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO getInviteeHelpResult(SocialActivityHelpResultDTO socialActivityHelpResultDTO, boolean isNewUser, String cookbookId, Long userId) {
        // 参团锁
        String lockKey = SocialConstant.JOIN_HELP_CONCURRENCE_LOCK_KEY + cookbookId;
        try {
            if (!redisLock.lock(lockKey)) {
                // 此次助力不成功，还原助力次数
                redisCache.increment(SocialConstant.SUCCESS_HELP_COUNT_KEY + !isNewUser + "_" + userId, -INTEGER_1);
                return socialActivityHelpResultDTO;
            }

            SocialMasterDO socialMasterCache = getSocialMaster(cookbookId);
            SocialMasterExtendDTO socialMasterExtendDTO = socialMasterCache.getSocialMasterExtendDTO();

            // 如果助力满员，则此次不可助力
            if (socialMasterExtendDTO.getTotalMember() < (socialMasterExtendDTO.getHelpCount() + INTEGER_1)) {
                String readyAmount = BigDecimal.valueOf((long) socialMasterExtendDTO.getWithdrawAmount()).divide(new BigDecimal(100)).toString();

                int helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE_FINISH.getCode();
                String helpCopywriterDesc = String.format(HelpCopywriterEnum.HELP_FAILURE_FINISH.getDesc(), readyAmount);
                if (isNewUser) {
                    helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_FAILURE_FINISH.getCode();
                    helpCopywriterDesc = String.format(HelpCopywriterEnum.NEW_USER_HELP_FAILURE_FINISH.getDesc(), readyAmount);
                }

                socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
                socialActivityHelpResultDTO.setHelpAmount(0);
                return socialActivityHelpResultDTO;
            }

            // 助力人和额外奖励
            socialActivityHelpResultDTO = createInviteeResult(socialActivityHelpResultDTO, isNewUser, cookbookId, userId, socialMasterCache, socialMasterExtendDTO);

            // 结束处理
            if (Objects.equals(socialMasterExtendDTO.getTotalMember(), socialMasterExtendDTO.getHelpCount())) {
                finishSocialActivity(socialMasterCache.getCreatorId(), cookbookId);
            }
            return socialActivityHelpResultDTO;
        } finally {
            redisLock.unlock(lockKey);
        }
    }

    /**
     * 活动结束处理
     *
     * @param userId
     * @param cookbookId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    private Boolean finishSocialActivity(Long userId, String cookbookId) {
        SocialMasterDO socialMasterDO = getSocialMaster(cookbookId);
        if (Objects.isNull(socialMasterDO)) {
            // 活动已结束
            return true;
        }

        SocialMasterExtendDTO socialMasterExtendDTO = socialMasterDO.getSocialMasterExtendDTO();
        Integer waitAmount = socialMasterExtendDTO.getWaitAmount();
        String currentStageStr = redisCache.get(SocialConstant.CURRENT_STAGE_KEY + cookbookId);
        int currentStage = NumberUtils.toInt(currentStageStr, INTEGER_1);
        JSONObject json = new JSONObject();
        json.put("cookbookId", cookbookId);
        json.put("userId", userId);
        json.put("currentStage", currentStage);
        if (waitAmount != 0) {
            json.put("amount", waitAmount);
            // 发送打款消息
            socialCommonService.sendMessage(SocialConstant.PAYING_AMOUNT_TOPIC, json.toJSONString());
        }
        if (socialMasterDO.getSocialMasterExtendDTO().getWithdrawAmount() == socialMasterDO.getTotalAmount()) {
            socialMasterDO.setHelpStatus(ActivityStatusEnum.FINISH.getCode());
            // 发送活动完成消息
            socialCommonService.sendMessage(SocialConstant.ACTIVITY_FINISH_TOPIC, json.toJSONString());
        } else {
            socialMasterDO.setHelpStatus(ActivityStatusEnum.EXPIRED.getCode());
            // 发送活动过期消息
            socialCommonService.sendMessage(SocialConstant.ACTIVITY_EXPIRED_TOPIC, json.toJSONString());
        }
        socialMasterExtendDTO.setReceiveAmount(socialMasterExtendDTO.getReceiveAmount() + waitAmount);
        socialMasterExtendDTO.setWaitAmount(0);
        socialMasterDO.setMasterConfig(JSON.toJSONString(socialMasterExtendDTO));
        int i = socialMasterService.updateSocialMasterByIdSelective(socialMasterDO);
        if (i > 0) {
            redisCache.delete(SocialConstant.SOCIAL_MASTER_INFO_KEY + cookbookId);
            return true;
        }
        return false;

    }

    /**
     * 助力结果
     *
     * @param socialActivityHelpResultDTO
     * @param isNewUser
     * @param cookbookId
     * @param userId
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO createInviteeResult(SocialActivityHelpResultDTO socialActivityHelpResultDTO, boolean isNewUser, String cookbookId, Long userId, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO) {
        try {
            SocialActivityConfigDTO socialActivityConfig = getSocialActivityConfig();
            // 助力阶段数以及对应需要邀请人数
            String stageMemberStr = redisCache.get(SocialConstant.HELP_STAGE_MEMBER_KEY + cookbookId);
            List<Integer> stageMemberList = JSONArray.parseArray(stageMemberStr, Integer.class);

            // 当前缓存阶段
            String currentStageStr = redisCache.get(SocialConstant.CURRENT_STAGE_KEY + cookbookId);
            int currentStage = NumberUtils.toInt(currentStageStr, INTEGER_1);

            // 好友助力金额
            int helpAmount = getHelpMoney(userId, socialMasterDO, socialActivityConfig, currentStage);
            if (helpAmount <= 0) {
                return getFailureInviteeHelpResult(socialActivityHelpResultDTO, isNewUser, cookbookId, userId, helpAmount);
            }

            SocialInviteeDO socialInviteeDO = createInvitee(helpAmount, cookbookId, userId, isNewUser, socialMasterDO, socialMasterExtendDTO, currentStage);
            if (Objects.isNull(socialInviteeDO)) {
                return getFailureInviteeHelpResult(socialActivityHelpResultDTO, isNewUser, cookbookId, userId, helpAmount);
            }

            // 好友助力金额
            String helpAmountStr = BigDecimal.valueOf(helpAmount).divide(new BigDecimal(INTEGER_100)).toString();
            int helpCopywriterCode = HelpCopywriterEnum.HELP_SUCCESS.getCode();
            String helpCopywriterDesc = String.format(HelpCopywriterEnum.HELP_SUCCESS.getDesc(), helpAmountStr);
            if (isNewUser) {
                // 新用户
                helpCopywriterCode = HelpCopywriterEnum.NEW_USER_HELP_SUCCESS.getCode();
                helpCopywriterDesc = String.format(HelpCopywriterEnum.NEW_USER_HELP_SUCCESS.getDesc(), helpAmountStr);
            }

            socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);

            if (SocialUtil.currentStageMemberSuccess(currentStage, socialMasterExtendDTO, stageMemberList)) {
                // 判断助力人数是否满足当前阶段且不为最后一个阶段
                // 额外奖励金额
                int premiumsAmount = getHelpMoney(null, socialMasterDO, socialActivityConfig, currentStage);
                if (premiumsAmount <= 0) {
                    return getFailureInviteeHelpResult(socialActivityHelpResultDTO, isNewUser, cookbookId, userId, premiumsAmount);
                }

                createPemiumsInvitee(cookbookId, userId, premiumsAmount, socialMasterDO, socialMasterExtendDTO, currentStage);
            }

            // 更新团长缓存数据库
            setSocialMasterDelCache(socialMasterDO, socialMasterExtendDTO);

            // 助力人缓存
            redisCache.setex(SocialConstant.JOIN_HELP_FLAG_KEY + cookbookId + "_" + userId, "INTEGER_1", DAYS_1, TimeUnit.DAYS);
            redisCache.setex(SocialConstant.HELP_INTERVAL_LIMIT_KEY + socialMasterDO.getCreatorId() + "_" + userId, String.valueOf(socialActivityConfig.getJoinIntervalDayLimit()), SocialUtil.getCustomdayLastTimeMillisecond(socialActivityConfig.getJoinIntervalDayLimit()), TimeUnit.MILLISECONDS);
            return socialActivityHelpResultDTO;
        } catch (Exception e) {
            log.error("助力结果异常 入参为cookbookId={},userId={},socialMasterDTO={}; error=", cookbookId, userId, JSON.toJSONString(socialMasterExtendDTO), e);
            return getFailureInviteeHelpResult(socialActivityHelpResultDTO, isNewUser, cookbookId, userId, 0);
        }
    }

    /**
     * 助力失败，给红包
     *
     * @param socialActivityHelpResultDTO
     * @param isNewUser
     * @param cookbookId
     * @param userId
     * @param helpAmount
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO getFailureInviteeHelpResult(SocialActivityHelpResultDTO socialActivityHelpResultDTO, Boolean isNewUser, String cookbookId, Long userId, int helpAmount) {
        // 助力失败，回滚助力人数据
        socialInviteeService.deleteHelpFailInvitee(cookbookId, userId);
        redisCache.increment(SocialConstant.SUCCESS_HELP_COUNT_KEY + !isNewUser + "_" + userId, -INTEGER_1);
        socialActivityHelpResultDTO.setHelpAmount(0);

        int helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE.getCode();
        String helpCopywriterDesc = HelpCopywriterEnum.HELP_FAILURE.getDesc();
        if (isNewUser) {
            helpCopywriterCode = HelpCopywriterEnum.HELP_FAILURE.getCode();
            helpCopywriterDesc = HelpCopywriterEnum.HELP_FAILURE.getDesc();
        }

        socialActivityHelpResultDTO = getSocialActivityResult(socialActivityHelpResultDTO, helpCopywriterCode, helpCopywriterDesc, cookbookId, userId);
        return socialActivityHelpResultDTO;
    }

    /**
     * 创建助力者信息
     *
     * @param helpAmount
     * @param cookbookId
     * @param userId
     * @param isNewUser
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param currentStage
     * @return com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO
     * @author zhonghuashishan
     */
    private SocialInviteeDO createInvitee(int helpAmount, String cookbookId, Long userId, boolean isNewUser, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, int currentStage) {
        // 获取用户的昵称和头像
        UserBaseInfoDTO userBaseInfoDTO = socialCommonService.getUserBaseByUserId(userId);
        String roundWord = WordRoundConstant.getRoundWord(helpAmount);

        SocialInviteeExtendDTO socialInviteeExtendDTO = new SocialInviteeExtendDTO();
        socialInviteeExtendDTO.setHelpAmountDoc(roundWord);
        socialInviteeExtendDTO.setUserId(userId);
        socialInviteeExtendDTO.setNewUserFlag(isNewUser);

        SocialInviteeDO socialInviteeDO = new SocialInviteeDO();
        socialInviteeDO.setCookbookId(cookbookId);
        socialInviteeDO.setInviteeId(userId);
        socialInviteeDO.setHelpAmount(helpAmount);
        socialInviteeDO.setInviteeNickName(userBaseInfoDTO.getPortrait());
        socialInviteeDO.setInviteeAvatar(userBaseInfoDTO.getNickName());
        socialInviteeDO.setHelpConfig(JSON.toJSONString(socialInviteeExtendDTO));
        socialInviteeDO.setCreateTime(new Date());
        socialInviteeDO.setUpdateTime(new Date());
        socialInviteeDO.setDelFlag(DelFlagEnum.EFFECTIVE.getCode());
        int successCount = socialInviteeService.insertSocialInvitee(socialInviteeDO);
        if (successCount > 0) {
            socialMasterExtendDTO.setHelpCount(socialMasterExtendDTO.getHelpCount() + INTEGER_1);
            setStageAmount(userId, cookbookId, socialMasterDO, socialMasterExtendDTO, helpAmount, 0, currentStage, false);
            return socialInviteeDO;
        }
        return null;
    }

    /**
     * 获取社交玩法结果
     *
     * @param socialActivityHelpResultDTO
     * @param helpCopywriterCode
     * @param helpCopywriterDesc
     * @param cookbookId
     * @param userId
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO getSocialActivityResult(SocialActivityHelpResultDTO socialActivityHelpResultDTO, Integer helpCopywriterCode, String helpCopywriterDesc, String cookbookId, Long userId) {
        try {
            socialActivityHelpResultDTO.setHelpCopywriterCode(helpCopywriterCode);
            socialActivityHelpResultDTO.setHelpCopywriterDesc(helpCopywriterDesc);

            Boolean isNewUser = socialCommonService.isNewUser(userId);

            // 新老用户助力成功或失败，展示红包
            String limitDays = redisCache.get(SocialConstant.SEND_COUPON_INTERVAL_LIMIT_KEY + cookbookId + userId);
            if (StringUtils.isEmpty(limitDays)) {
                SocialActivityConfigDTO socialActivityConfig = getSocialActivityConfig();
                redisCache.expireAt(SocialConstant.SEND_COUPON_INTERVAL_LIMIT_KEY + cookbookId + userId, SocialUtil.getNextDay(socialActivityConfig.getJoinIntervalDayLimit()));

                getCouponInfo(isNewUser, userId, socialActivityHelpResultDTO);
                String couponCode = socialActivityHelpResultDTO.getCouponCode();
                if (helpCopywriterCode == HelpCopywriterEnum.HELP_SUCCESS.getCode()
                        || helpCopywriterCode == HelpCopywriterEnum.NEW_USER_HELP_SUCCESS.getCode()
                        || helpCopywriterCode == HelpCopywriterEnum.NEW_USER_HELP_SUCCESS.getCode()) {
                    // 助力成功直接发红包
                    socialCommonService.sendCoupon(userId, couponCode);
                    return socialActivityHelpResultDTO;
                }

                /*
                 助力失败判断是否有红包（用户账户红包券码是否有效）
                    1、有效：直接展示
                    2、无效：发券后展示
                 */
                UserCouponResultDTO userCouponInfo = socialCommonService.getUserCouponInfo(userId, couponCode);
                if (Objects.isNull(userCouponInfo)) {
                    // 发送优惠券
                    socialCommonService.sendCoupon(userId, couponCode);
                    return socialActivityHelpResultDTO;
                }

                // 判断用户账户红包券码是否有效（未使用 且 有效截止日期大于当前时间）
                Integer couponStatus = userCouponInfo.getCouponStatus();
                if (!Objects.equals(CouponStatusEnum.NOT_USE.getCode(), couponStatus)) {
                    // 发送优惠券
                    socialCommonService.sendCoupon(userId, couponCode);
                }

                // 获取用户账户有效红包直接返回结果
                socialActivityHelpResultDTO.setMoneyOffCost(userCouponInfo.getMoneyOffCost());
                socialActivityHelpResultDTO.setAllowanceCost(userCouponInfo.getAllowanceCost());
                return socialActivityHelpResultDTO;
            }

            SocialInviteeDO socialInviteeDO = socialInviteeService.selectSocialInviteeByCondition(cookbookId, userId);
            if (isFinish(cookbookId)) {
                // 已完成，显示团长已领的金额
                socialActivityHelpResultDTO = getCouponInfo(isNewUser, userId, socialActivityHelpResultDTO);
            } else if (Objects.nonNull(socialInviteeDO)) {
                // 再次进入，该团存在助力值（已助力/达助力上限/不能给同一好友助力/命中风控）
                SocialInviteeExtendDTO socialInviteeExtendDTO = socialInviteeDO.getSocialInviteeExtendDTO();
                socialActivityHelpResultDTO.setHelpAmount(socialInviteeDO.getHelpAmount());
                socialActivityHelpResultDTO = getCouponInfo(isNewUser, userId, socialActivityHelpResultDTO);
            } else {
                // 再次进入，该团不存在助力值（已助力/达助力上限/不能给同一好友助力/命中风控）
                socialActivityHelpResultDTO = getCouponInfo(isNewUser, userId, socialActivityHelpResultDTO);
            }
        } catch (Exception e) {
            log.error("社交玩法异常 入参为userId={},cookbookId={},helpCopywriterCode={},error=", userId, cookbookId, helpCopywriterCode, e);
        }

        return socialActivityHelpResultDTO;
    }

    /**
     * 额外奖励参团信息
     *
     * @param cookbookId
     * @param userId
     * @param premiumsAmount
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param currentStage
     * @return void
     * @author zhonghuashishan
     */
    private void createPemiumsInvitee(String cookbookId, Long userId, int premiumsAmount, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, int currentStage) {
        SocialInviteeDO socialInviteeDO = new SocialInviteeDO();
        SocialInviteeExtendDTO socialInviteeExtendDTO = new SocialInviteeExtendDTO();

        String roundWord = WordRoundConstant.getRoundWord(premiumsAmount);
        socialInviteeExtendDTO.setUserId(userId);
        socialInviteeExtendDTO.setHelpAmountDoc(roundWord);
        socialInviteeExtendDTO.setPremiums(true);
        socialInviteeExtendDTO.setNewUserFlag(false);
        socialInviteeDO.setCookbookId(cookbookId);
        socialInviteeDO.setInviteeId(0L);
        socialInviteeDO.setInviteeNickName(SocialConstant.PREMIUMS_NICKNAME);
        socialInviteeDO.setInviteeAvatar(SocialConstant.PREMIUMS_AVATAR);
        socialInviteeDO.setHelpAmount(premiumsAmount);
        socialInviteeDO.setCreateTime(new Date());
        socialInviteeDO.setUpdateTime(new Date());
        socialInviteeDO.setHelpConfig(JSON.toJSONString(socialInviteeExtendDTO));
        socialInviteeDO.setDelFlag(DelFlagEnum.EFFECTIVE.getCode());
        int successCount = socialInviteeService.insertSocialInvitee(socialInviteeDO);
        if (successCount > 0) {
            setStageAmount(userId, cookbookId, socialMasterDO, socialMasterExtendDTO, 0, premiumsAmount, currentStage, true);
            // 设置当前阶段+1到缓存
            redisCache.set(SocialConstant.CURRENT_STAGE_KEY + cookbookId, String.valueOf(currentStage + INTEGER_1), 0);
            // 当前阶段是否弹额外奖励金额，弹完之后删除，取最新的额外奖励金额
            redisCache.setex(SocialConstant.CURRENT_STAGE_PREMIUMS_KEY + cookbookId, String.valueOf(premiumsAmount), DAYS_1, TimeUnit.DAYS);
        }
    }

    /**
     * 计算本次公共金额详情
     *
     * @param userId
     * @param cookbookId
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @param helpAmount            本次助力金额
     * @param premiumsAmount        阶段额外奖励金额
     * @param stageFlag             是否满足阶段：true-满足，false-不满足
     * @param stageFlag
     * @return com.ruyuan.careerplan.social.domain.entity.SocialMasterDO
     * @author zhonghuashishan
     */
    private SocialMasterDO setStageAmount(long userId, String cookbookId, SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO, int helpAmount, int premiumsAmount, int currentStageStr, boolean stageFlag) {
        // 待到账金额，原有的金额+本次助力金额+额外奖励金额 （注：满足阶段后清空）
        socialMasterExtendDTO.setWaitAmount(socialMasterExtendDTO.getWaitAmount() + helpAmount + premiumsAmount);

        if (stageFlag && socialMasterExtendDTO.getWaitAmount() > 0) {
            // 调用微信充值接口 生产消息
            // 充值金额 socialMasterDTO.getWaitAmount()

            JSONObject json = new JSONObject();
            json.put("userId", socialMasterDO.getCreatorId());
            json.put("amount", socialMasterExtendDTO.getWaitAmount());
            json.put("currentStage", currentStageStr);
            json.put("cookbookId", cookbookId);
            socialCommonService.sendMessage(SocialConstant.PAYING_AMOUNT_TOPIC, json.toJSONString());

            // 已到账金额 单位分 第2次群分享+满足阶段提现（判断是否满足，满足则清空待到账金额） 30
            socialMasterExtendDTO.setReceiveAmount(socialMasterExtendDTO.getReceiveAmount() + socialMasterExtendDTO.getWaitAmount());
            // 待到账金额 单位分 清空
            socialMasterExtendDTO.setWaitAmount(0);
        }

        // 已提的金额 单位分 （微信充值成功 第2次群分享+满足阶段提现）+待到账金额
        socialMasterExtendDTO.setWithdrawAmount(socialMasterExtendDTO.getReceiveAmount() + socialMasterExtendDTO.getWaitAmount());
        // 待提现金额 单位分 总金额-已提的金额
        socialMasterExtendDTO.setRemainAmount(socialMasterDO.getTotalAmount() - socialMasterExtendDTO.getWithdrawAmount());
        socialMasterDO.setSocialMasterExtendDTO(socialMasterExtendDTO);
        return socialMasterDO;
    }

    /**
     * 获取团信息
     *
     * @param cookbookId
     * @return com.ruyuan.careerplan.social.domain.entity.SocialMasterDO
     * @author zhonghuashishan
     */
    private SocialMasterDO getSocialMaster(String cookbookId) {
        String socialMasterStr = redisCache.get(SocialConstant.SOCIAL_MASTER_INFO_KEY + cookbookId);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(socialMasterStr)) {
            JSONObject jsonObject = JSON.parseObject(socialMasterStr);
            return JSON.toJavaObject(jsonObject, SocialMasterDO.class);
        }

        SocialMasterDO socialMasterDO = socialMasterService.selectSocialMasterByCookbookId(cookbookId);
        if (Objects.nonNull(socialMasterDO)) {
            redisCache.setex(SocialConstant.SOCIAL_MASTER_INFO_KEY + cookbookId, JSON.toJSONString(socialMasterDO), DAYS_1, TimeUnit.DAYS);
        }

        return socialMasterDO;
    }

    /**
     * 更新团长缓存数据库
     *
     * @param socialMasterDO
     * @param socialMasterExtendDTO
     * @return boolean
     * @author zhonghuashishan
     */
    private boolean setSocialMasterDelCache(SocialMasterDO socialMasterDO, SocialMasterExtendDTO socialMasterExtendDTO) {
        socialMasterDO.setMasterConfig(JSON.toJSONString(socialMasterExtendDTO));
        int successCount = socialMasterService.updateSocialMasterByIdSelective(socialMasterDO);
        if (successCount > 0) {
            redisCache.delete(SocialConstant.SOCIAL_MASTER_INFO_KEY + socialMasterDO.getCookbookId());
            return true;
        }
        return false;
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

    /**
     * 获取优惠券
     *
     * @param isNewUser
     * @param userId
     * @param socialActivityHelpResultDTO
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO getCouponInfo(Boolean isNewUser, Long userId, SocialActivityHelpResultDTO socialActivityHelpResultDTO) {
        SocialActivityConfigDTO socialActivityConfig = getSocialActivityConfig();
        if (isNewUser) {
            socialActivityHelpResultDTO.setCouponCode(socialActivityConfig.getNewUserCouponCode());
            socialActivityHelpResultDTO.setCouponUrl(socialActivityConfig.getNewUserCouponUrl());
            socialActivityHelpResultDTO.setShowCouponWin(true);
        } else {
            socialActivityHelpResultDTO = setOldUserCouponCode(socialActivityConfig, userId, socialActivityHelpResultDTO);
        }
        CouponInfoDTO couponInfoDTO = socialCommonService.getCouponInfoByCouponCode(socialActivityHelpResultDTO.getCouponCode());
        if (Objects.nonNull(couponInfoDTO)) {
            socialActivityHelpResultDTO.setMoneyOffCost(couponInfoDTO.getMoneyOffCost());
            socialActivityHelpResultDTO.setAllowanceCost(couponInfoDTO.getAllowanceCost());
        }

        return socialActivityHelpResultDTO;
    }

    /**
     * 老用户优惠券设置
     *
     * @param socialActivityConfigDTO
     * @param userId
     * @param socialActivityHelpResultDTO
     * @return com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO
     * @author zhonghuashishan
     */
    private SocialActivityHelpResultDTO setOldUserCouponCode(SocialActivityConfigDTO socialActivityConfigDTO, long userId, SocialActivityHelpResultDTO socialActivityHelpResultDTO) {
        socialActivityHelpResultDTO.setCouponCode(socialActivityConfigDTO.getOldUserCouponCode());
        socialActivityHelpResultDTO.setCouponUrl(socialActivityConfigDTO.getOldUserCouponUrl());
        socialActivityHelpResultDTO.setShowCouponWin(false);
        return socialActivityHelpResultDTO;
    }

    /**
     * 发放优惠券
     *
     * @param userId
     * @param couponCode
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    private Boolean sendCoupon(Long userId, String couponCode) {
        try {
            return socialCommonService.sendCoupon(userId, couponCode);
        } catch (Exception e) {
            log.error("发放优惠券异常 入参为userId={},couponCode={},error=", userId, couponCode, e);
        }
        return false;
    }

    /**
     * 判断是否能够进行助力
     *
     * @param cookbookId
     * @return boolean
     * @author zhonghuashishan
     */
    private boolean isFinish(String cookbookId) {
        SocialMasterDO socialMasterDO = getSocialMaster(cookbookId);
        return ActivityStatusEnum.UNDERWAY.getCode() != socialMasterDO.getHelpStatus();
    }

    /**
     * 新老用户参团上限
     *
     * @param isNewUser
     * @param cookbookId
     * @return int
     * @author zhonghuashishan
     */
    private int helpCountLimit(Boolean isNewUser, String cookbookId) {
        SocialActivityConfigDTO socialActivityConfig = getSocialActivityConfig();
        if (isNewUser) {
            return socialActivityConfig.getNewUserJoinUpperLimit();
        }

        return socialActivityConfig.getJoinUpperLimit();
    }

    /**
     * 计算金额
     *
     * @param userId
     * @param socialMasterDO
     * @param socialActivityConfig
     * @param currentStage
     * @return int
     * @author zhonghuashishan
     */
    public int getHelpMoney(Long userId, SocialMasterDO socialMasterDO, SocialActivityConfigDTO socialActivityConfig, int currentStage) {
        if (Objects.isNull(socialMasterDO) || Objects.isNull(socialActivityConfig)) {
            return 0;
        }

        // 如果是最后一个人，把剩下所有金额都给他
        SocialMasterExtendDTO socialMasterExtendDTO = socialMasterDO.getSocialMasterExtendDTO();
        if (userId != null && (socialMasterExtendDTO.getHelpCount() + INTEGER_1) == socialMasterExtendDTO.getTotalMember()) {
            return socialMasterExtendDTO.getRemainAmount();
        }

        // 如果userId为空，则为额外金额。否则为助力金额
        if (userId == null) {
            return getRandom(socialActivityConfig.getShareExtraLowerLimit(), socialActivityConfig.getShareExtraUpperLimit());
        }

        Boolean isNewUser = socialCommonService.isNewUser(userId);
        if (isNewUser) {
            return getRandom(socialActivityConfig.getNewUserHelpLowerLimit(), socialActivityConfig.getNewUserHelpUpperLimit());
        }
        return getRandom(socialActivityConfig.getOldUserHelpLowerLimit(), socialActivityConfig.getOldUserHelpUpperLimit());
    }

    /**
     * 随机数
     *
     * @param start
     * @param end
     * @return int
     * @author zhonghuashishan
     */
    private static int getRandom(int start, int end) {
        return random.nextInt(end) % (end - start + INTEGER_1) + start;
    }

    /**
     * 初始化社交活动
     *
     * 本活动由后端进行初始化，意味着哪些活动可以进行分享获取奖励
     *
     * @param userId
     * @param cookbookId
     * @return boolean
     * @author zhonghuashishan
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean initSocialActivity(Long userId, String cookbookId) {
        String lockKey = SocialConstant.INIT_SOCIAL_ACTIVITY_LOCK_KEY;
        try {
            if(!redisLock.lock(lockKey)) {
                return false;
            }

            SocialMasterDO socialMasterDO = getSocialMaster(cookbookId);
            if (Objects.nonNull(socialMasterDO)) {
                // 活动已存在，不可重复初始化
                log.warn("活动已存在，不可重复初始化");
                return false;
            }

            Integer totalAmount = INTEGER_300;
            UserBaseInfoDTO userBaseInfoDTO = socialCommonService.getUserBaseByUserId(userId);
            SocialMasterExtendDTO socialMasterExtendDTO = new SocialMasterExtendDTO();
            socialMasterExtendDTO.setTotalMember(INTEGER_5);
            socialMasterExtendDTO.setWaitAmount(0);
            socialMasterExtendDTO.setWithdrawAmount(0);
            socialMasterExtendDTO.setRemainAmount(totalAmount);
            socialMasterExtendDTO.setReceiveAmount(0);
            socialMasterExtendDTO.setHelpCount(0);
            socialMasterExtendDTO.setShareTwoPayingAmount(false);
            socialMasterDO = new SocialMasterDO();
            socialMasterDO.setCookbookId(cookbookId);
            socialMasterDO.setCreatorId(userId);
            socialMasterDO.setHelpStatus(ActivityStatusEnum.WAITING.getCode());
            socialMasterDO.setMasterAvatar(userBaseInfoDTO.getPortrait());
            socialMasterDO.setMasterNickname(userBaseInfoDTO.getNickName());
            socialMasterDO.setTotalAmount(totalAmount);
            socialMasterDO.setMasterConfig(JSON.toJSONString(socialMasterExtendDTO));
            socialMasterDO.setSocialMasterExtendDTO(socialMasterExtendDTO);
            socialMasterDO.setDelFlag(DelFlagEnum.EFFECTIVE.getCode());
            int i = socialMasterService.insertSocialMaster(socialMasterDO);
            if (i > 0) {
                List<Integer> memberCountList = stageMemberCount(INTEGER_5);
                redisCache.setex(SocialConstant.HELP_STAGE_MEMBER_KEY + cookbookId, JSON.toJSONString(memberCountList), DAYS_2, TimeUnit.DAYS);
                redisCache.setex(SocialConstant.SOCIAL_MASTER_INFO_KEY + cookbookId, JSON.toJSONString(socialMasterDO), DAYS_2, TimeUnit.DAYS);
                redisCache.setex(SocialConstant.CURRENT_STAGE_KEY + cookbookId, String.valueOf(INTEGER_1), DAYS_2, TimeUnit.DAYS);
                return true;
            }
        } finally {
            redisLock.unlock(lockKey);
        }

        return false;
    }

    /**
     * 计算每个阶段的人数（顺序排列）
     *
     * 1、人数小于第一阶段，全部放入阶段一
     * 2、人数大于第四阶段，全部放入阶段五
     *
     * @param memberCount
     * @return
     */
    private List<Integer> stageMemberCount(int memberCount) {
        SocialActivityConfigDTO socialActivityConfigDTO = getSocialActivityConfig();
        int stageOneMember = socialActivityConfigDTO.getStageOneMember();
        int stageTwoMember = socialActivityConfigDTO.getStageTwoMember();
        int stageThreeMember = socialActivityConfigDTO.getStageThreeMember();
        int stageFourMember = socialActivityConfigDTO.getStageFourMember();
        List<Integer> stageList = Lists.newArrayList();
        if(memberCount <= stageOneMember) {
            stageList.add(memberCount);
        } else if(memberCount <= (stageOneMember+stageTwoMember)) {
            stageList.add(stageOneMember);
            stageList.add(memberCount-stageOneMember);
        } else if(memberCount <= (stageOneMember+stageTwoMember+stageThreeMember)) {
            stageList.add(stageOneMember);
            stageList.add(stageTwoMember);
            stageList.add(memberCount-stageOneMember-stageTwoMember);
        } else if(memberCount <= (stageOneMember+stageTwoMember+stageThreeMember+stageFourMember)) {
            stageList.add(stageOneMember);
            stageList.add(stageTwoMember);
            stageList.add(stageThreeMember);
            stageList.add(memberCount-stageOneMember-stageTwoMember-stageThreeMember);
        } else {
            stageList.add(stageOneMember);
            stageList.add(stageTwoMember);
            stageList.add(stageThreeMember);
            stageList.add(stageFourMember);
            stageList.add(memberCount-stageOneMember-stageTwoMember-stageThreeMember-stageFourMember);
        }
        return stageList;
    }
}
