package com.ruyuan.careerplan.goodscart.mq.consumer.listener;

import com.alibaba.fastjson.JSON;
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
public class CookbookCartPersistenceListener implements MessageListenerConcurrently {

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
                log.info("执行购物车加购落库消息逻辑，消息内容：{}", messageExt.getBody());
                String msg = new String(messageExt.getBody());
                CookBookCartDO cartDO = JSON.parseObject(msg, CookBookCartDO.class);

                // 用户购物车每一个商品条目，加入进去的时候，异步化的落库
                // mysql数据库有点像是你的备用存储一样，异步同步数据的备份数据存储一样
                // 一般来说购物车的主数据存储，是通过redis来实现的，写和读，redis，没有不一致的问题
                // 异步化的备用数据存储，万一redis集群全盘都崩溃了以后，这个时候就会导致说，我们的购物车的主数据都没了
                // 可以基于mysql数据库来进行降级，降级提供购物车的写和读，等缓存恢复了以后，再进行缓存prewarn预热的加载，数据库里的数据再加载到缓存里去
                // 缓存雪崩的问题和解决方案，现在来说，还不用考虑他
                log.info("购物车数据开始保存到MySQL，userId: {}, cartDO: {}", cartDO.getUserId(), msg);
                cookBookCartDAO.save(cartDO);
            }
        } catch (Exception e) {
            log.error("consume error, 购物车落库消息消费失败", e);
            // 本次消费失败，下次重新消费
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        log.info("购物车加购持久化消息消费成功, result: {}", ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
