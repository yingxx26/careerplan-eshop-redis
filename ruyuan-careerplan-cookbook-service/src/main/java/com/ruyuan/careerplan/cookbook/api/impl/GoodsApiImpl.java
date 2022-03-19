package com.ruyuan.careerplan.cookbook.api.impl;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.cookbook.api.GoodsApi;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateSkuDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateSkuRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuInfoQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuSaleableRequest;
import com.ruyuan.careerplan.cookbook.exception.CookbookBizException;
import com.ruyuan.careerplan.cookbook.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 商品服务
 *
 * @author zhonghuashishan
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = GoodsApi.class, retries = 0)
public class GoodsApiImpl implements GoodsApi {

    @Autowired
    private GoodsService goodsService;

    @Override
    public JsonResult<SaveOrUpdateSkuDTO> saveOrUpdateSku(SaveOrUpdateSkuRequest request) {
        try {
            SaveOrUpdateSkuDTO dto = goodsService.saveOrUpdateSku(request);
            return JsonResult.buildSuccess(dto);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<SkuInfoDTO> getSkuInfoBySkuId(SkuInfoQueryRequest request) {
        try {
            SkuInfoDTO dto = goodsService.getSkuInfoBySkuId(request);
            return JsonResult.buildSuccess(dto);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<List<SkuInfoDTO>> listSkuInfo(SkuInfoQueryRequest request) {
        try {
            List<SkuInfoDTO> skuInfoDTOS = goodsService.listSkuInfo(request);
            return JsonResult.buildSuccess(skuInfoDTOS);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<Boolean> skuIsSaleable(SkuSaleableRequest request) {
        try {
            Boolean result = goodsService.skuIsSaleable(request);
            return JsonResult.buildSuccess(result);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<Boolean> skuUpOrDown(SkuSaleableRequest request) {
        try {
            goodsService.skuUpOrDown(request);
            return JsonResult.buildSuccess();
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
}
