package com.ruyuan.careerplan.inventory.service;


import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;

import java.math.BigDecimal;

/**
 *
 *
 * @author zhonghuashishan
 */
public interface InventoryService {

    /**
     * 商品出入库
     * @param request
     */
    void putStorage(InventoryRequest request);

    /**
     * 扣减，返还商品库存
     * @param request
     */
    void deductProductStock(InventoryRequest request);
    /**
     *  查询当前商品的剩余库存
     * @param skuId 入参查询条件
     * @return 商品数量
     */
    BigDecimal queryProductStock(Long skuId);


    /**
     * 库存变化存储记录
     * @param request
     */
    void updateInventory(InventoryRequest request);

    /**
     * 执行库存同步缓存的lua脚本逻辑
     * @param request
     */
    void executeStockLua(InventoryRequest request);
}
