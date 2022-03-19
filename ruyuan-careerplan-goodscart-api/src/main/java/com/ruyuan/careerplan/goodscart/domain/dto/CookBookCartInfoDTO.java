package com.ruyuan.careerplan.goodscart.domain.dto;

import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
public class CookBookCartInfoDTO implements Serializable {

    /**
     * 未失效的购物车商品列表
     */
    private List<CartSkuInfoDTO> skuList;

    /**
     * 失效的购物车商品列表
     */
    private List<CartSkuInfoDTO> disabledSkuList;

    /**
     * 结算价格信息
     */
    private BillingDTO billing;
}
