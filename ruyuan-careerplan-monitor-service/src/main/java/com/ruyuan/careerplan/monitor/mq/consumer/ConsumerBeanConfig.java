package com.ruyuan.careerplan.monitor.mq.consumer;

import com.ruyuan.careerplan.monitor.constants.RocketMqConstant;
import com.ruyuan.careerplan.monitor.mq.config.RocketMQProperties;
import com.ruyuan.careerplan.monitor.mq.consumer.listener.CookbookEvictedKeyMonitorListener;
import com.ruyuan.careerplan.monitor.mq.consumer.listener.CookbookLargeKeyMonitorListener;
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
     * redis大key binlog 消费者
     * @param cookbookLargeKeyMonitorListener
     * @return
     * @throws MQClientException
     */
    @Bean("cookbookLargeKeyMonitorTopic")
    public DefaultMQPushConsumer receiveLargeKeyMonitorConsumer(CookbookLargeKeyMonitorListener cookbookLargeKeyMonitorListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.BINLOG_MONITOR_LARGE_KEY_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.BINLOG_MONITOR_LARGE_KEY_TOPIC, "*");
        consumer.registerMessageListener(cookbookLargeKeyMonitorListener);
        consumer.start();
        return consumer;
    }


    /**
     * 模拟商品服务的消费者，过滤包含tags中包含goodsInfo的消息
     * @param cookbookEvictedKeyMonitorListener
     * @return
     * @throws MQClientException
     */
    @Bean("cookbookEvictedKeyMonitorTopic")
    public DefaultMQPushConsumer receiveEvictedKeyMonitorConsumer(CookbookEvictedKeyMonitorListener cookbookEvictedKeyMonitorListener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.EVICTED_MONITOR_KEY_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.EVICTED_MONITOR_KEY_TOPIC, "goodsInfo");
        consumer.registerMessageListener(cookbookEvictedKeyMonitorListener);
        consumer.start();
        return consumer;
    }

}
