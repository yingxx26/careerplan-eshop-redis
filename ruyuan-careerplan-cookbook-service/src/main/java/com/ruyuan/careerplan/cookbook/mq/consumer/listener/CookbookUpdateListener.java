package com.ruyuan.careerplan.cookbook.mq.consumer.listener;

import com.ruyuan.careerplan.common.cache.CacheSupport;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.cookbook.constants.RedisKeyConstants;
import com.ruyuan.careerplan.cookbook.dao.CookbookDAO;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.message.CookbookUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CookbookUpdateListener implements MessageListenerConcurrently {

    private static final int PAGE_SIZE = 20;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private CookbookDAO cookbookDAO;

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
                log.info("执行作者菜谱缓存数据更新逻辑，消息内容：{}", messageExt.getBody());
                String msg = new String(messageExt.getBody());

                CookbookUpdateMessage message = JsonUtil.json2Object(msg, CookbookUpdateMessage.class);

                Long userId = message.getUserId();

                String cookbookUpdateLockKey = RedisKeyConstants.COOKBOOK_UPDATE_LOCK_PREFIX + userId;
                redisLock.blockedLock(cookbookUpdateLockKey);

                try {
                    // 这个时候我们需要在这里对userid的菜谱list页缓存都去做一个重建

                    String userCookbookCountKey = RedisKeyConstants.USER_COOKBOOK_COUNT_PREFIX + userId;
                    Integer count = Integer.valueOf(redisCache.get(userCookbookCountKey));
                    int pageNums = count / PAGE_SIZE + 1;

                    for(int pageNo = 1; pageNo <= pageNums; pageNo++) {
                        String userCookbookPageKey = RedisKeyConstants.USER_COOKBOOK_PAGE_PREFIX
                                + userId + ":" + pageNo;
                        String cookbooksJson = redisCache.get(userCookbookPageKey);
                        if(cookbooksJson == null || "".equals(cookbooksJson)) {
                            continue;
                        }

                        // 确实有这一页数据，此时就需要对这一页数据缓存去进行更新
                        List<CookbookDTO> cookbooks = cookbookDAO.pageByUserId(userId, pageNo, PAGE_SIZE);
                        redisCache.set(userCookbookPageKey,
                                JsonUtil.object2Json(cookbooks),
                                CacheSupport.generateCacheExpireSecond());

                        // 只要缓存异步实现了更新操作，用户发表完了菜谱以后，去菜谱list分页，但凡是之前有缓存的page
                        // 都会看到page最新的数据，就算是有一些延迟，也不会太高的
                    }
                } finally {
                    redisLock.unlock(cookbookUpdateLockKey);
                }

                // 又可能会出现并发更新缓存的问题，数据库和缓存不一致的问题
                // 更新菜谱的时候，这里的每一页缓存都会重建，读取page的时候，也可能会去更新的页缓存
                // 不做任何的措施的话，导致两个地方都有菜谱page缓存写入的操作，并发的问题

                // 也可以直接通过CookbookConsistencyListener监听binlog。然后删除缓存

//                List<CookbookDTO> cookbookDTOS = cookbookDAO.listByUserId(userId);
//                log.info("作者菜谱缓存数据更新，userId: {}", userId);
//
//                String userCookbookKey = RedisKeyConstants.USER_COOKBOOK_PREFIX + userId;
//                redisCache.delete(userCookbookKey);
//                redisCache.lPushAll(userCookbookKey, JsonUtil.listObject2ListJson(cookbookDTOS));
            }
        } catch (Exception e) {
            // 本次消费失败，下次重新消费
            log.error("consume error, 更新作者菜谱缓存数据消费失败", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        log.info("更新作者菜谱缓存数据消费成功, result: {}", ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}