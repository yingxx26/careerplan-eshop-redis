package com.ruyuan.careerplan.cookbook.domain.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
public class SkuSaleableRequest implements Serializable {
    /**
     * 商品编码
     */
    private Long skuId;

   /**
     * 商品数量
     */
    private Integer count;

    /**
     * 仓库地址
     */
    private String warehouse;
    /**
     * 商品上架状态
     */
    private Integer status;
}
