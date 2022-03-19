package com.ruyuan.careerplan.goodscart.mq.consumer.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.goodscart.dao.CookBookCartDAO;
import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCartDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class CookbookCartUpdateListener implements MessageListenerConcurrently {

    @Autowired
    private CookBookCartDAO cookBookCartDAO;

    /**
     * 并发消费消息
     *
     * @param msgList
     * @param context
     * @return
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt messageExt : msgList) {
                log.info("执行购物车持久化更新消息逻辑，消息内容：{}", messageExt.getBody());
                String msg = new String(messageExt.getBody());
                CookBookCartDO cartDO = JSON.parseObject(msg, CookBookCartDO.class);

                log.info("购物车数据开始更新到MySQL，userId: {}, cartDO: {}", cartDO.getUserId(), msg);
                UpdateWrapper<CookBookCartDO> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("count", cartDO.getCount());
                updateWrapper.eq("user_id", cartDO.getUserId());
                updateWrapper.eq("sku_id", cartDO.getSkuId());
                if (cartDO.getCount() == 0) {
                    cookBookCartDAO.remove(updateWrapper);
                    continue;
                }
                cookBookCartDAO.update(updateWrapper);
            }
        } catch (Exception e) {
            // 本次消费失败，下次重新消费
            log.error("consume error, 购物车持久化更新消息消费失败", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        log.info("购物车更新持久化消息消费成功, result: {}", ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
