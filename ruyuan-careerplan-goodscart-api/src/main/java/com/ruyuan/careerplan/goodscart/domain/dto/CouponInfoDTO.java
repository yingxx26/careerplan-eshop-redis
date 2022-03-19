package com.ruyuan.careerplan.goodscart.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponInfoDTO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券金额
     */
    private Integer couponAmount;

    /**
     * 门槛（除无门槛卷外，其他劵应满足此条件才能优惠）
     */
    private Integer threshold;

    /**
     * 状态 0-生效；1-失效；2-过期
     */
    private Integer status;

    /**
     * 优惠券说明
     */
    private String remark;
}
