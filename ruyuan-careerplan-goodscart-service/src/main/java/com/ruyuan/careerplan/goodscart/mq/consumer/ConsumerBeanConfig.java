package com.ruyuan.careerplan.goodscart.mq.consumer;

import com.ruyuan.careerplan.goodscart.constants.RocketMqConstant;
import com.ruyuan.careerplan.goodscart.mq.config.RocketMQProperties;
import com.ruyuan.careerplan.goodscart.mq.consumer.listener.CookbookCartCheckedStatusListener;
import com.ruyuan.careerplan.goodscart.mq.consumer.listener.CookbookCartPersistenceListener;
import com.ruyuan.careerplan.goodscart.mq.consumer.listener.CookbookCartUpdateListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Configuration
public class ConsumerBeanConfig {

    /**
     * 配置内容对象
     */
    @Autowired
    private RocketMQProperties rocketMQProperties;

    /**
     * 购物车商品落库消费者
     * @param cookbookCartPersistenceListener
     * @return
     * @throws MQClientException
     */
    @Bean("cookbookCartAsyncPersistenceTopic")
    public DefaultMQPushConsumer receiveCartPersistenceConsumer(CookbookCartPersistenceListener cookbookCartPersistenceListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.COOKBOOK_ASYNC_PERSISTENCE_MESSAGE_SEND_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.COOKBOOK_ASYNC_PERSISTENCE_MESSAGE_SEND_TOPIC, "*");
        consumer.registerMessageListener(cookbookCartPersistenceListener);
        consumer.start();
        return consumer;
    }

    /**
     * 购物车商品更新消费者
     * @param cookbookCartUpdateListener
     * @return
     * @throws MQClientException
     */
    @Bean("cookbookCartAsyncUpdateTopic")
    public DefaultMQPushConsumer receiveCartUpdateConsumer(CookbookCartUpdateListener cookbookCartUpdateListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.COOKBOOK_ASYNC_UPDATE_MESSAGE_SEND_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.COOKBOOK_ASYNC_UPDATE_MESSAGE_SEND_TOPIC, "*");
        consumer.registerMessageListener(cookbookCartUpdateListener);
        consumer.start();
        return consumer;
    }


    /**
     * 购物车商品选中状态更新消费者
     * @param cookbookCartCheckedStatusListener
     * @return
     * @throws MQClientException
     */
    @Bean("cookbookCartAsyncCheckedStatusTopic")
    public DefaultMQPushConsumer receiveCartCheckedStatusConsumer(CookbookCartCheckedStatusListener cookbookCartCheckedStatusListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.COOKBOOK_ASYNC_CHECKED_STATUS_MESSAGE_SEND_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.COOKBOOK_ASYNC_CHECKED_STATUS_MESSAGE_SEND_TOPIC, "*");
        consumer.registerMessageListener(cookbookCartCheckedStatusListener);
        consumer.start();
        return consumer;
    }

}
