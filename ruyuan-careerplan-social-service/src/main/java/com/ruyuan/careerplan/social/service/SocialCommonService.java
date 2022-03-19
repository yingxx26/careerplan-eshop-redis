package com.ruyuan.careerplan.social.service;

import com.ruyuan.careerplan.social.domain.dto.CouponInfoDTO;
import com.ruyuan.careerplan.social.domain.dto.UserBaseInfoDTO;
import com.ruyuan.careerplan.social.domain.dto.UserCouponResultDTO;

/**
 * 此服务模拟各个中台提供接口，调用后返回业务结果
 *
 * @author zhonghuashishan
 */
public interface SocialCommonService {

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
    Long getUserIdByToken(String accessToken);

    /**
     * 是否新用户
     *
     * 模拟订单中台接口
     *  1、有支付订单记录即为老用户，否则为新用户
     *  2、这里不做订单中台业务，只提供设计思路
     *
     * @param userId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    Boolean isNewUser(Long userId);

    /**
     * 是否被风控
     *
     * 模拟风控中台接口
     *  1、先获取用户中台WeChatOpenId
     *  2、根据WeChatOpenId判断用户在平台中的账号风控等级
     *  3、结合风控平台本身的用户画像，给出最终是否被风控
     *  4、这里不做用户平台、风控平台业务，只提供设计思路
     *
     * @param userId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    Boolean isRiskUser(Long userId, String ip);

    /**
     * 获取用户信息
     *
     * 模拟用户中台接口
     *
     * @param userId
     * @return com.ruyuan.careerplan.social.domain.dto.UserBaseInfoDTO
     * @author zhonghuashishan
     */
    UserBaseInfoDTO getUserBaseByUserId(Long userId);


    /**
     * 获取优惠券信息
     *
     * 模拟营销中台接口
     *
     * @param couponCode
     * @return com.ruyuan.careerplan.social.domain.dto.CouponInfoDTO
     * @author zhonghuashishan
     */
    CouponInfoDTO getCouponInfoByCouponCode(String couponCode);

    /**
     * 获取用户账户下的优惠券
     *
     * 模拟营销中台接口
     *
     * @param userId
     * @param couponCode
     * @return com.ruyuan.careerplan.social.domain.dto.UserCouponInfoDTO
     * @author zhonghuashishan
     */
    UserCouponResultDTO getUserCouponInfo(Long userId, String couponCode);

    /**
     * 发送优惠券
     *
     * 模拟营销中台接口
     *
     * @param userId
     * @param couponCode
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    Boolean sendCoupon(Long userId, String couponCode);

    /**
     * 发送消息
     *
     * @param topic
     * @param message
     * @return void
     * @author zhonghuashishan
     */
    void sendMessage(String topic, String message);

}
