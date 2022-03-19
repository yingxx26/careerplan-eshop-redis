package com.ruyuan.careerplan.inventory.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 库存操作入口
 * @author zhonghuashishan
 */
@Data
public class InventoryRequest implements Serializable {
    /**
     * 入库单号
     */
    private String warehouseCode;
    /**
     * 商品SKU
     */
    private Long skuId;
    /**
     * 库存数量 正数是加库存，负数是扣减库存
     */
    private Integer inventoryNum;
    /**
     * 仓库地址
     */
    private String warehouse;
    /**
     * 操作人
     */
    private Integer operator;


}
