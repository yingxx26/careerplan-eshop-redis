package com.ruyuan.careerplan.goodscart.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookBookCartSaleRequest implements Serializable {
    private Long userId;
    private Long skuId;
}
