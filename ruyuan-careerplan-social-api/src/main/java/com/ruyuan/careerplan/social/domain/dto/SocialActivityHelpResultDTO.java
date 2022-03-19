package com.ruyuan.careerplan.social.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SocialActivityHelpResultDTO implements Serializable {

    /**
     * 助力金额
     */
    private Integer helpAmount;

    /**
     * 优惠券使用门槛（例如：满100减30；单位：分）
     * 设置0则为无门槛
     */
    private Integer moneyOffCost;

    /**
     * 优惠券减额（实际要减掉的价格；单位：分）
     */
    private Integer allowanceCost;

    /**
     * 红包券码
     */
    private String couponCode;

    /**
     * 优惠券跳转地址
     */
    private String couponUrl;

    /**
     * 优惠券状态（0-未使用，1-已使用，2-已过期）
     */
    private Integer couponStatus;

    /**
     * 助力文案code
     */
    private Integer helpCopywriterCode;

    /**
     * 助力文案
     */
    private String helpCopywriterDesc;

    /**
     * 是否显示新用户弹框
     */
    private Boolean showCouponWin;

}
