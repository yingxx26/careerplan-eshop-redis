package com.ruyuan.careerplan.goodscart.service.impl;

import com.ruyuan.careerplan.goodscart.constants.RocketMqConstant;
import com.ruyuan.careerplan.goodscart.mq.producer.DefaultProducer;
import com.ruyuan.careerplan.goodscart.service.MessagePushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class MessagePushServiceImpl implements MessagePushService {

    @Autowired
    private DefaultProducer defaultProducer;

    @Deprecated
    @Override
    public void pushSaleMessage(Long skuId) {
        defaultProducer.sendMessage(RocketMqConstant.COOKBOOK_SHOPPING_CART_SALE_MESSAGE_SEND_TOPIC,
                String.valueOf(skuId), "购物车商品降价通知消息");
        log.info("发送购物车商品降价通知消息, skuId: {}", skuId);
    }
}
