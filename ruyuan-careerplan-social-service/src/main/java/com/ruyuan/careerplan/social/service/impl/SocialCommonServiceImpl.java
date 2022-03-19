package com.ruyuan.careerplan.social.service.impl;

import com.google.common.collect.Lists;
import com.ruyuan.careerplan.social.domain.dto.CouponInfoDTO;
import com.ruyuan.careerplan.social.domain.dto.UserBaseInfoDTO;
import com.ruyuan.careerplan.social.domain.dto.UserCouponResultDTO;
import com.ruyuan.careerplan.social.domain.dto.UserCouponRelationDTO;
import com.ruyuan.careerplan.social.enums.CouponStatusEnum;
import com.ruyuan.careerplan.social.service.SocialCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 此服务模拟各个中台提供接口，调用后返回业务结果
 *
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class SocialCommonServiceImpl implements SocialCommonService {

    /**
     * 用户中台：老用户清单
     */
    private static List<Long> oldUserList = Lists.newArrayList(1001L, 1002L, 1003L, 1004L);

    /**
     * 用户中台：用户授权的AccessToken对应用户ID
     */
    private static Map<String, Long> accessTokenUserMap = new HashMap<String, Long>() {{
        put("accesstokenaaa", 1001L);
        put("accesstokenbbb", 1002L);
        put("accesstokenccc", 1003L);
        put("accesstokenddd", 1004L);
        put("accesstokeneee", 1005L);
        put("accesstokenfff", 1006L);
        put("accesstokenggg", 1007L);
        put("accesstokenhhh", 1008L);
    }};

    /**
     * 用户中台：用户授权的WeChatOpenId
     */
    private static Map<Long, String> wechatOpenIdMap = new HashMap<Long, String>() {{
        put(1001L, "wechatopenidaaa");
        put(1002L, "wechatopenidbbb");
        put(1003L, "wechatopenidccc");
        put(1004L, "wechatopenidddd");
        put(1005L, "wechatopenideee");
        put(1006L, "wechatopenidfff");
        put(1007L, "wechatopenidggg");
        put(1008L, "wechatopenidhhh");
    }};

    /**
     * 用户中台：用户信息
     */
    private static Map<Long, UserBaseInfoDTO> userBaseInfoMap = new HashMap<Long, UserBaseInfoDTO>() {{
        put(1001L, new UserBaseInfoDTO(1001L, "儒猿用户A", "http://imgurl1"));
        put(1002L, new UserBaseInfoDTO(1002L, "儒猿用户B", "http://imgurl2"));
        put(1003L, new UserBaseInfoDTO(1003L, "儒猿用户C", "http://imgurl3"));
        put(1004L, new UserBaseInfoDTO(1004L, "儒猿用户D", "http://imgurl4"));
        put(1005L, new UserBaseInfoDTO(1005L, "儒猿用户E", "http://imgurl5"));
        put(1006L, new UserBaseInfoDTO(1006L, "儒猿用户F", "http://imgurl6"));
        put(1007L, new UserBaseInfoDTO(1007L, "儒猿用户G", "http://imgurl7"));
        put(1008L, new UserBaseInfoDTO(1008L, "儒猿用户H", "http://imgurl8"));
    }};

    /**
     * 社区电商中心：用户对应的菜谱
     */
    private static Map<Long, List<String>> userCookbookMap = new HashMap<Long, List<String>>() {{
        put(1001L, Lists.newArrayList("cookbooka1", "cookbooka2"));
        put(1002L, Lists.newArrayList("cookbookb1"));
        put(1003L, Lists.newArrayList("cookbookc1", "cookbookc2"));
        put(1004L, Lists.newArrayList("cookbookd1"));
    }};

    /**
     * 风控中台：风控指标越高越危险
     * 详情可查看微信官方风控文档：https://developers.weixin.qq.com/minigame/dev/guide/open-ability/security.html
     */
    private static Map<String, Integer> wechatOpenIdRiskMap = new HashMap<String, Integer>() {{
        put("wechatopenidaaa", 1);
        put("wechatopenidbbb", 2);
        put("wechatopenidccc", 3);
        put("wechatopenidddd", 4);
    }};

    /**
     * 风控中台：IP黑名单
     */
    private static List<String> riskBlackList = Lists.newArrayList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4");

    /**
     * 营销中台：优惠券信息
     */
    private static Map<String, CouponInfoDTO> couponInfoMap = new HashMap<String, CouponInfoDTO>() {{
        put("couponaaa", new CouponInfoDTO("couponaaa", 0, 2));
        put("couponbbb", new CouponInfoDTO("couponbbb", 20, 5));
        put("couponccc", new CouponInfoDTO("couponccc", 50, 10));
        put("couponddd", new CouponInfoDTO("couponddd", 100, 30));
    }};

    /**
     * 营销中台：用户领取的优惠券信息
     */
    private static Map<Long, UserCouponRelationDTO> userCouponMap = new HashMap<Long, UserCouponRelationDTO>() {{
        put(1001L, new UserCouponRelationDTO(1001L, Lists.newArrayList(new CouponInfoDTO("couponaaa", 0, 2))));
        put(1002L, new UserCouponRelationDTO(1002L, Lists.newArrayList(new CouponInfoDTO("couponbbb", 20, 5))));
        put(1003L, new UserCouponRelationDTO(1003L, Lists.newArrayList(new CouponInfoDTO("couponccc", 50, 10))));
        put(1004L, new UserCouponRelationDTO(1004L, Lists.newArrayList(new CouponInfoDTO("couponddd", 100, 30))));
    }};

    /**
     * 获取用户ID
     *
     * 模拟用户中台接口
     *  1、前端http请求传入授权accessToken
     *  2、根据token换取用户ID
     *
     * @param accessToken
     * @return java.lang.Long
     * @author zhonghuashishan
     */
    @Override
    public Long getUserIdByToken(String accessToken){
        return accessTokenUserMap.get(accessToken);
    }

    /**
     * 是否是新用户
     *
     * 模拟订单中台接口
     *  1、有支付订单记录即为老用户，否则为新用户
     *  2、这里不做订单中台业务，只提供设计思路
     *
     * @param userId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    @Override
    public Boolean isNewUser(Long userId) {
        if(oldUserList.contains(userId)) {
            return false;
        }
        return true;
    }

    /**
     * 是否被风控
     *
     * 模拟风控中台接口
     * 1、先获取用户中台WeChatOpenId
     * 2、根据WeChatOpenId判断用户在平台中的账号风控等级
     * 3、结合风控平台本身的用户画像，给出最终是否被风控
     * 4、这里不做用户平台、风控平台业务，只提供设计思路
     *
     * @param userId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    @Override
    public Boolean isRiskUser(Long userId, String ip) {
        if(riskBlackList.contains(ip)) {
            // 命中风控中台IP黑名单，直接被风控
            return true;
        }

        String wechatOpenId = wechatOpenIdMap.get(userId);
        Integer riskLevel = wechatOpenIdRiskMap.get(wechatOpenId);
        if(Objects.isNull(riskLevel)) {
            return false;
        }
        if(riskLevel > 3) {
            /*
             1、高度可疑的风险，建议根据业务逻辑直接拦截。例如，红包类活动返回不中奖或最小额红包；打榜类活动不计算票数；登录/注册操作要求二次验证；高危业务可选择限制本次操作；
             2、结合风控平台本身的用户画像，给出最终是否被风控（这里给出被风控结果）；
             */
            return true;
        }
        return false;
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return com.ruyuan.careerplan.social.domain.dto.UserBaseInfoDTO
     * @author zhonghuashishan
     */
    @Override
    public UserBaseInfoDTO getUserBaseByUserId(Long userId) {
        return userBaseInfoMap.get(userId);
    }

    /**
     * 获取优惠券信息
     *
     * @param couponCode
     * @return com.ruyuan.careerplan.social.domain.dto.CouponInfoDTO
     * @author zhonghuashishan
     */
    @Override
    public CouponInfoDTO getCouponInfoByCouponCode(String couponCode) {
        return couponInfoMap.get(couponCode);
    }

    /**
     * 获取用户账户下的优惠券
     *
     * @param userId
     * @return com.ruyuan.careerplan.social.domain.dto.UserCouponRelationDTO
     * @author zhonghuashishan
     */
    @Override
    public UserCouponResultDTO getUserCouponInfo(Long userId, String couponCode) {
        UserCouponRelationDTO userCouponRelationDTO = userCouponMap.get(userId);
        if(Objects.isNull(userCouponRelationDTO) || CollectionUtils.isEmpty(userCouponRelationDTO.getCouponInfoDTOList())) {
            return null;
        }

        List<CouponInfoDTO> couponInfoDTOList = userCouponRelationDTO.getCouponInfoDTOList();
        for (CouponInfoDTO couponInfoDTO : couponInfoDTOList) {
            if(Objects.equals(couponCode, couponInfoDTO.getCouponCode())) {
                UserCouponResultDTO userCouponResultDTO = new UserCouponResultDTO();
                BeanUtils.copyProperties(couponInfoDTO, userCouponResultDTO);
                userCouponResultDTO.setUserId(userId);
                // 模拟不同用户的优惠券状态
                userCouponResultDTO.setCouponStatus(userId.intValue() % 3);
                return userCouponResultDTO;
            }
        }
        return null;
    }

    /**
     * 发送优惠券
     *
     * 模拟营销中台发券接口
     *  1、营销中台判断是否给用户发券
     *  2、将发券结果返回
     *
     * @param userId
     * @param couponCode
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    @Override
    public Boolean sendCoupon(Long userId, String couponCode) {
        try {
            // 判断用户是否已领过此券且未使用
            UserCouponResultDTO userCouponInfo = getUserCouponInfo(userId, couponCode);
            if(Objects.nonNull(userCouponInfo) && Objects.equals(CouponStatusEnum.NOT_USE, userCouponInfo.getCouponStatus())) {
                return Boolean.FALSE;
            }

            // 给用户发券
            List<CouponInfoDTO> couponInfoDTOList = Lists.newArrayList();
            CouponInfoDTO couponInfoDTO = couponInfoMap.get(couponCode);
            couponInfoDTOList.add(couponInfoDTO);
            UserCouponRelationDTO userCouponRelationDTO = new UserCouponRelationDTO();
            userCouponRelationDTO.setUserId(userId);
            userCouponRelationDTO.setCouponInfoDTOList(couponInfoDTOList);
            userCouponMap.put(userId, userCouponRelationDTO);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("发送优惠券异常 入参为userId={},couponCode={},error=", userId, couponCode, e);
            return Boolean.FALSE;
        }
    }

    /**
     * 发送消息
     * 模拟生产消息接口
     *  1、账务中台消费，给用户微信账号打款
     *
     *
     * @param topic
     * @param message
     * @return void
     * @author zhonghuashishan
     */
    @Override
    public void sendMessage(String topic, String message) {
        log.info("发送消息");
    }


}
