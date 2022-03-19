package com.ruyuan.careerplan.social.domain.dto;

import lombok.Data;

/**
 * 参团信息
 *
 * @author zhonghuashishan
 */
@Data
public class SocialInviteeExtendDTO {

    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否新用户
     */
    private  Boolean newUserFlag;
    
    /**
     * 助力金额随机文案
     */
    private String helpAmountDoc;

    /**
     * 是否额外奖励
     */
    private Boolean premiums;

    /**
     * 优惠券券码
     */
    private String couponCode;

    /**
     * 优惠券跳转地址
     */
    private String couponUrl;

}
