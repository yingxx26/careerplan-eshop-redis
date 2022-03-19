package com.ruyuan.careerplan.inventory.api;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;

import java.math.BigDecimal;

/**
 * 库存服务接口
 *
 * @author zhonghuashishan
 */
public interface InventoryApi {
    /**
     * 商品出入库
     * @param request
     */
    JsonResult<Boolean> putStorage(InventoryRequest request);

    /**
     * 扣减，返还商品库存
     * @param request
     */
    JsonResult<Boolean> deductProductStock(InventoryRequest request);

    /**
     *  查询当前商品的剩余库存
     * @param skuId 入参查询条件
     * @return 商品数量
     */
    JsonResult<BigDecimal> queryProductStock(Long skuId);
}
