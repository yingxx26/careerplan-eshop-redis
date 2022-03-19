package com.ruyuan.careerplan.inventory.controller;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.inventory.api.InventoryApi;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 库存中心控制类
 *
 * @author zhonghuashishan
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @DubboReference(version = "1.0.0")
    private InventoryApi inventoryApi;

    /**
     * 用户商品库存入库
     */
    @PostMapping("/putStorage")
    public JsonResult<Boolean> putStorage(@RequestBody InventoryRequest request) {
        return inventoryApi.putStorage(request);
    }
    /**
     * 扣减，返还商品库存
     */
    @PostMapping("/deductProductStock")
    public JsonResult<Boolean> deductProductStock(@RequestBody InventoryRequest request) {
        return inventoryApi.deductProductStock(request);
    }

    /**
     * 查询当前商品的剩余库存
     */
    @GetMapping("/queryProductStock")
    public JsonResult<BigDecimal> queryProductStock(Long skuId) {
        return inventoryApi.queryProductStock(skuId);
    }


}
