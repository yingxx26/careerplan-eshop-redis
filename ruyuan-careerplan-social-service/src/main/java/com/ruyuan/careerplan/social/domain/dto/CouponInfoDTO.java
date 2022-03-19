package com.ruyuan.careerplan.social.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券信息
 *
 * @author zhonghuashishan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponInfoDTO {

    /**
     * 优惠券编码
     */
    private String couponCode;

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
