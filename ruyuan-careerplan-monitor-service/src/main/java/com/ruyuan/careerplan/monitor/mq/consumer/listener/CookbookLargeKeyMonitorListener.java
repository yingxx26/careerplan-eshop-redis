package com.ruyuan.careerplan.monitor.mq.consumer.listener;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.careerplan.common.domain.BinlogDataDTO;
import com.ruyuan.careerplan.common.utils.BinlogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class CookbookLargeKeyMonitorListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String msg = new String(messageExt.getBody());
                // 解析binlog数据模型
                BinlogDataDTO binlogData = BinlogUtils.getBinlogData(msg);
                log.info("消费到binlog消息, binlogData: {}", binlogData);

                /*
                    这里已经将监测出来的大key的binlog数据消费到了

                    binlog中包含了大key的db索引，key的名称、大小、类型等信息

                    剩下的步骤就是给开发人员发送大key信息的通知了
                 */

                // 推送通知
                informByPush(binlogData);
            }
        } catch (Exception e) {
            // 本次消费失败，下次重新消费
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 第三方平台推送消息到app
     *
     * @param binlogData
     */
    private void informByPush(BinlogDataDTO binlogData){
        log.info("消息推送中：消息内容：{}", binlogData);
    }

}
