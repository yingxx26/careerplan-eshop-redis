package com.ruyuan.careerplan.goodscart.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
public class BillingDTO implements Serializable {
    /**
     * 合计金额
     */
    private Integer totalPrice;

    /**
     * 已优惠金额
     */
    private Integer salePrice;
}
