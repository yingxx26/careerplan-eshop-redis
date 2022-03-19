package com.ruyuan.careerplan.goodscart.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedOptimalCouponDTO implements Serializable {
    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券金额
     */
    private Integer couponAmount;
}
