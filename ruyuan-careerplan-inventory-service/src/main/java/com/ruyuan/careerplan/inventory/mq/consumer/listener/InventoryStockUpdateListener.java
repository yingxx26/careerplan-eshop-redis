package com.ruyuan.careerplan.inventory.mq.consumer.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.inventory.constants.RedisKeyConstants;
import com.ruyuan.careerplan.inventory.dao.InventoryDAO;
import com.ruyuan.careerplan.inventory.dao.StorageDetailLogDAO;
import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
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
public class InventoryStockUpdateListener implements MessageListenerConcurrently {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private RedisLock redisLock;

    /**
     * 并发消费消息
     *
     * @param msgList
     * @param context
     * @return
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext context) {
           String skuInfoLockKey = "";
        try {
            for (MessageExt messageExt : msgList) {
                String msg = new String(messageExt.getBody());
                InventoryRequest request  = JSON.parseObject(msg, InventoryRequest.class);

                skuInfoLockKey = RedisKeyConstants.INVENTORY_LOCK_PREFIX + request.getSkuId();
                // 每条库存的日志变更涉及明细，异步场景下，需要按顺序进行扣减，避免库存明细数据不准确
                boolean lock = redisLock.tryLock(skuInfoLockKey,3000L);
                if (lock){
                    // 存储库存变化记录
                    inventoryService.updateInventory(request);
                } else {
                    log.error("consume failure,消息待下次重试");
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        } catch (Exception e) {
            // 本次消费失败，下次重新消费
            log.error("consume error, 库存变更消息消费失败", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        } finally {
            redisLock.unlock(skuInfoLockKey);
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
