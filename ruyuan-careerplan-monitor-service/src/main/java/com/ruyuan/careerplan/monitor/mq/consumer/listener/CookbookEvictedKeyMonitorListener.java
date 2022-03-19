package com.ruyuan.careerplan.monitor.mq.consumer.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 模拟商品服务的消费者
 *
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class CookbookEvictedKeyMonitorListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String msg = new String(messageExt.getBody());
                log.info("消费到evicted消息, tags: {}, keys: {}, msg: {}", messageExt.getTags(), messageExt.getKeys(), msg);

                // 消费tags中包含goodsInfo的消息，然后按照自己的业务去处理逻辑

                log.info("商品服务进行对淘汰key的业务处理...");
            }
        } catch (Exception e) {
            // 本次消费失败，下次重新消费
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
