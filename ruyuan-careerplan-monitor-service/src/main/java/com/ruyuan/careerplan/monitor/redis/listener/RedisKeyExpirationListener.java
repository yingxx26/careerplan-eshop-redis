package com.ruyuan.careerplan.monitor.redis.listener;

import com.google.common.base.Charsets;
import com.ruyuan.careerplan.monitor.constants.RocketMqConstant;
import com.ruyuan.careerplan.monitor.mq.producer.DefaultProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private DefaultProducer defaultProducer;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据回收事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 回收的key
        String evictedKey = message.toString();
        // 配对模式
        String patternString = new String(pattern, Charsets.UTF_8);
        if (StringUtils.isEmpty(evictedKey)) {
            log.warn("收到evicted事件, 但是key值不正确, key: {}, pattern: {}", evictedKey, patternString);
            return;
        }
        log.info("收到evicted事件, key: {}", evictedKey);

        String[] split = evictedKey.split(":");
        String tags = "";
        if (split.length == 0) {
            tags = evictedKey;
        } else {
            tags = split[0];
        }

        /*
         * 假设被淘汰的key是goodsInfo:600000011
         * 那么消息的tags是goodsInfo
         * 消息的key就是goodsInfo:600000011
         */
        defaultProducer.sendMessage(RocketMqConstant.EVICTED_MONITOR_KEY_TOPIC, evictedKey, tags, evictedKey, "evicted");
        log.info("发送消息成功, topic: {}, tags: {}, key: {}, msg: {}", RocketMqConstant.EVICTED_MONITOR_KEY_TOPIC, tags, evictedKey, evictedKey);
    }
}
