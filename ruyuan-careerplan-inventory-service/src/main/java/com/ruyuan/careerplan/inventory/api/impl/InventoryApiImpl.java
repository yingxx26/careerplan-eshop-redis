package com.ruyuan.careerplan.inventory.api.impl;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.inventory.api.InventoryApi;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import com.ruyuan.careerplan.inventory.exception.InventoryBizException;
import com.ruyuan.careerplan.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author zhonghuashishan
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = InventoryApi.class)
public class InventoryApiImpl implements InventoryApi {

    @Autowired
    private InventoryService inventoryService;
    /**
     * 商品出入库
     * @param request
     */
    @Override
    public JsonResult<Boolean> putStorage(InventoryRequest request) {
        try {
            inventoryService.putStorage(request);
            return JsonResult.buildSuccess();
        } catch (InventoryBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
    /**
     * 扣减，返还商品库存
     * @param request
     */
    @Override
    public JsonResult<Boolean> deductProductStock(InventoryRequest request) {
        try {
            inventoryService.deductProductStock(request);
            return JsonResult.buildSuccess();
        } catch (InventoryBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
    /**
     *  查询当前商品的剩余库存
     * @param skuId 入参查询条件
     * @return 商品数量
     */
    @Override
    public JsonResult<BigDecimal> queryProductStock(Long  skuId) {
        try {
            BigDecimal sumNum = inventoryService.queryProductStock(skuId);
            return JsonResult.buildSuccess(sumNum);
        } catch (InventoryBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(skuId), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(skuId), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
}


