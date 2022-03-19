package com.ruyuan.careerplan.cookbook.mq.consumer.listener;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ruyuan.careerplan.common.domain.BinlogDataDTO;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.utils.BinlogUtils;
import com.ruyuan.careerplan.cookbook.enums.BinlogType;
import com.ruyuan.careerplan.cookbook.enums.ConsistencyTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 处理数据一致性的消息队列
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class CookbookConsistencyListener implements MessageListenerConcurrently {


    @Autowired
    private RedisCache redisCache;
    /**
     * 处理mysql的binlog变化，处理对应的需要清理的缓存key
     * @param list
     * @param consumeConcurrentlyContext
     * @return
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String msg = new String(messageExt.getBody());
                // 解析binlog数据模型，并过滤掉查询
                BinlogDataDTO binlogData = buildBinlogData(msg);
                // 获取binlog的模型，获取本次变化的表名称，在本地配置常量类里面匹配对应的缓存key前缀以及缓存标识字段，非配置的表不进行处理
                String cacheKey = filterConsistencyTable(binlogData);
                // 删除该key的缓存
                deleteCacheKey(cacheKey);
            }
        } catch (Exception e) {
            log.error("consume error, 缓存清理失败", e);
            // 本次消费失败，下次重新消费
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 对缓存进行清理
     * @param cacheKey
     */
    private void deleteCacheKey(String cacheKey) {
        if (StringUtils.isBlank(cacheKey)){
            return;
        }
        redisCache.delete(cacheKey);
    }

    /**
     * 过滤掉目前不需要处理的表的Binlog，并返回组装所需的缓存key
     * @param binlogData
     * @return
     */
    private String filterConsistencyTable(BinlogDataDTO binlogData) {
        if (Objects.isNull(binlogData)){
            return null;
        }
        String tableName = binlogData.getTableName();
        List<Map<String, Object>> dataList = binlogData.getDataMap();
        // 获取配置的常量映射的具体配置
        ConsistencyTableEnum consistencyTableEnum = ConsistencyTableEnum.findByEnum(tableName);
        if (Objects.isNull(consistencyTableEnum)){
            return null;
        }
        String cacheValue = "";
        if (CollectionUtils.isNotEmpty(dataList)){
            Map<String, Object> dataMap = dataList.get(0);

            cacheValue = dataMap.get(consistencyTableEnum.getCacheField())+"";
        }
        if (StringUtils.isBlank(cacheValue)){
            return null;
        }
        // 获取配置的缓存前缀key + 当前的标识字段，组装缓存key
        return consistencyTableEnum.getCacheKey() + cacheValue;
    }

    /**
     * 解析binlog的数据模型，并过滤掉 查询的Binlog
     * @param msg
     * @return
     */
    private BinlogDataDTO buildBinlogData(String msg){
        // 先解析binlog的对象，转换为模型
        BinlogDataDTO binlogData = BinlogUtils.getBinlogData(msg);
        // 模型为null，则直接返回
        if (Objects.isNull(binlogData)){
            return null;
        }
        Boolean isOperateType = BinlogType.INSERT.getValue().equals(binlogData.getOperateType())
                || BinlogType.DELETE.getValue().equals(binlogData.getOperateType())
                || BinlogType.UPDATE.getValue().equals(binlogData.getOperateType());
        //  只保留增删改的binlog对象,如果数据对象为空则也不处理
        if (!isOperateType || CollectionUtils.isEmpty(binlogData.getDataMap())){
            return null;
        }
        // 返回解析好的可用模型
        return binlogData;
    }
}
