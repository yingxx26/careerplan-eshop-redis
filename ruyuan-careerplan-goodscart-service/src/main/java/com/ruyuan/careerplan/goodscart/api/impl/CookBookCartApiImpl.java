package com.ruyuan.careerplan.goodscart.api.impl;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.goodscart.api.CookBookCartApi;
import com.ruyuan.careerplan.goodscart.exception.CookbookCartBizException;
import com.ruyuan.careerplan.goodscart.service.CookBookCartService;
import com.ruyuan.careerplan.goodscart.service.MessagePushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhonghuashishan
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = CookBookCartApi.class, retries = 0)
public class CookBookCartApiImpl implements CookBookCartApi {

    @Autowired
    private MessagePushService messagePushService;

    @Autowired
    private CookBookCartService cookBookCartService;

    @Override
    public JsonResult<String> pushSaleMessage(Long skuId) {
        try {
            messagePushService.pushSaleMessage(skuId);
            return JsonResult.buildSuccess("success");
        } catch (CookbookCartBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(skuId), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(skuId), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<Integer> calculateCartPrice(Long userId) {
        try {
            Integer price = cookBookCartService.calculateCartPrice(userId);
            return JsonResult.buildSuccess(price);
        } catch (CookbookCartBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(userId), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(userId), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
}
