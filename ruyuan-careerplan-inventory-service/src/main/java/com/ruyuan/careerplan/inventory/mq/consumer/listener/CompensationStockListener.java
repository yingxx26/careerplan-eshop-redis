package com.ruyuan.careerplan.inventory.mq.consumer.listener;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.inventory.dao.MqIdempotentDAO;
import com.ruyuan.careerplan.inventory.domain.entity.MqIdempotentLogDO;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import com.ruyuan.careerplan.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class CompensationStockListener implements MessageListenerConcurrently {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private MqIdempotentDAO mqIdempotentDAO;
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : msgList) {
                log.info("库存同步缓存消费，消息内容：{}", messageExt.getBody());
                String msg = new String(messageExt.getBody());
                // 保存消息的记录，用来处理幂等，一个消息只能被消费一次
                SaveIdempotentMq(messageExt.getMsgId());

                InventoryRequest request  = JSON.parseObject(msg, InventoryRequest.class);
                inventoryService.executeStockLua(request);
            }
        }catch (Exception e){
            // 默认每次消息都为成功，重发消息在同步缓存里面进行处理
            log.error("consume error, 库存同步缓存消息消费失败", e);
        }
        // 默认只消费一次
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 记录消息，保证一个消息只能被消费一次
     * @param msgId
     */
    private void SaveIdempotentMq(String msgId){
        MqIdempotentLogDO mqIdempotentLogDO  = new MqIdempotentLogDO();
        mqIdempotentLogDO.setMsgId(msgId);
        mqIdempotentDAO.save(mqIdempotentLogDO);
    }
}
