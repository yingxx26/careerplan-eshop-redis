package com.ruyuan.careerplan.social.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户的优惠券信息
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponResultDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券编码
     */
    private String couponCode;

    /**
     * 优惠券状态（0-未使用，1-已使用，2-已过期）
     */
    private Integer couponStatus;

    /**
     * 优惠券使用门槛（例如：满100减30；单位：分）
     * 设置0则为无门槛
     */
    private Integer moneyOffCost;

    /**
     * 优惠券减额（实际要减掉的价格；单位：分）
     */
    private Integer allowanceCost;
    
}
