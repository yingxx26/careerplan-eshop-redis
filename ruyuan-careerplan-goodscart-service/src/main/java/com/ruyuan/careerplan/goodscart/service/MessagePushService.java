package com.ruyuan.careerplan.goodscart.service;

/**
 * @author zhonghuashishan
 */
public interface MessagePushService {

    /**
     * 推送商品降价通知消息
     *
     * @param skuId
     */
    void pushSaleMessage(Long skuId);

}
