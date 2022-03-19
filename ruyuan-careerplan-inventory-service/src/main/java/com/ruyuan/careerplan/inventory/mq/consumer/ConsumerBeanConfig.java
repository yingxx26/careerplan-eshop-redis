package com.ruyuan.careerplan.inventory.mq.consumer;

import com.ruyuan.careerplan.inventory.constants.RocketMqConstant;
import com.ruyuan.careerplan.inventory.mq.config.RocketMQProperties;
import com.ruyuan.careerplan.inventory.mq.consumer.listener.CompensationStockListener;
import com.ruyuan.careerplan.inventory.mq.consumer.listener.InventoryStockUpdateListener;
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
     * 商品库存扣减变更的topic
     * @param inventoryStockUpdateListener
     * @return
     * @throws MQClientException
     */
    @Bean("inventoryStockUpdateTopic")
    public DefaultMQPushConsumer inventoryStockUpdateConsumer(InventoryStockUpdateListener inventoryStockUpdateListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.PUSH_DEFAULT_PRODUCER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.INVENTORY_PRODUCT_STOCK_TOPIC, "*");
        consumer.registerMessageListener(inventoryStockUpdateListener);
        consumer.start();
        return consumer;
    }

    /**
     * 商品库存同步缓存的补偿topic
     * @param compensationStockListener
     * @return
     * @throws MQClientException
     */
    @Bean("compensationStockListenerTopic")
    public DefaultMQPushConsumer compensationStockListenerTopic(CompensationStockListener compensationStockListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.PUSH_COMPENSATION_PRODUCER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.COMPENSATION_PRODUCT_STOCK_TOPIC, "*");
        consumer.registerMessageListener(compensationStockListener);
        consumer.start();
        return consumer;
    }
}
