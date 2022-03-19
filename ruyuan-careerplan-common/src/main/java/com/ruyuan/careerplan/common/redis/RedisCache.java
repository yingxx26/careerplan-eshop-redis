package com.ruyuan.careerplan.common.redis;

import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class RedisCache {

    private RedisTemplate redisTemplate;

    public RedisCache(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 缓存存储
     *
     * @param key
     * @param value
     * @param seconds
     * @return void
     * @author zhonghuashishan
     */
    public void set(String key, String value, int seconds){
        ValueOperations<String,String> vo = redisTemplate.opsForValue();
        if(seconds > 0){
            vo.set(key, value, seconds, TimeUnit.SECONDS);
        }else{
            vo.set(key, value);
        }
    }

    /**
     * 缓存存储
     * 将数据存储在内存和redis中，如果不是热key，就只存储redis
     *
     * @param key
     * @param value
     * @param seconds
     * @return void
     * @author zhonghuashishan
     */
    public void setCache(String key, Object value, int seconds){
        /*
         * 方法给热key赋值value，如果是热key，该方法才会赋值，非热key，什么也不做
         * 如果是热key，存储在内存中
         */
        JdHotKeyStore.smartSet(key, value);
        this.set(key, JsonUtil.object2Json(value), seconds);
    }

    /**
     * 缓存获取
     *
     * @param key
     * @return java.lang.String
     * @author zhonghuashishan
     */
    public String get(String key){
        ValueOperations<String,String> vo = redisTemplate.opsForValue();
        return vo.get(key);
    }

    /**
     * 缓存获取
     * 首先尝试从内存中获取，内存中没有则从redis中获取
     *
     * @param key
     * @return java.lang.String
     * @author zhonghuashishan
     */
    public Object getCache(String key){
        /*
         * 从内存中获取商品信息
         * 如果是热key，则存在两种情况，1是返回value，2是返回null。
         * 返回null是因为尚未给它set真正的value，返回非null说明已经调用过set方法了，本地缓存value有值了。
         * 如果不是热key，则返回null，并且将key上报到探测集群进行数量探测。
         */
        Object hotkeyValue = JdHotKeyStore.getValue(key);
        log.info("从内存中获取信息,key:{},value:{}", key, hotkeyValue);
        if (hotkeyValue != null) {
            return hotkeyValue;
        }
        String value = this.get(key);
        log.info("从缓存中获取信息,key:{},value:{}", key, value);
        /*
         * 方法给热key赋值value，如果是热key，该方法才会赋值，非热key，什么也不做
         * 如果是热key，存储在内存中
         * 每次从缓存中获取数据后都尝试往热key中放一下
         * 如果不放，则在成为热key之前，将数据放入缓存中了，但是没放到内存中。
         * 如果此时变成热key了，但是下次查询内存没查到，查缓存信息，查到了，就直接返回了，内存中就没有数据
         */
        JdHotKeyStore.smartSet(key, value);
        return value;
    }



    /**
     * 缓存手动失效
     *
     * @param key
     * @return boolean
     * @author zhonghuashishan
     */
    public boolean delete(String key){
        return redisTemplate.delete(key);
    }

    /**
     * 缓存存储并设置过期时间
     *
     * @param key
     * @param value
     * @param time
     * @return void
     * @author zhonghuashishan
     */
    public void setex(String key, String value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);

    }

    /**
     * 缓存批量获取
     *
     * @param keyList
     * @return java.util.List
     * @author zhonghuashishan
     */
    public List mget(List<String> keyList) {
        return redisTemplate.opsForValue().multiGet(keyList);
    }

    /**
     * 删除key下的多个值
     *
     * @param key
     * @param values
     * @return void
     * @author zhonghuashishan
     */
    public void srem(String key, String[] values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 删除key下的多个值
     *
     * @param key
     * @param values
     * @return void
     * @author zhonghuashishan
     */
    public void sadd(String key, String[] values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 缓存成员获取
     *
     * @param key
     * @return java.util.Set
     * @author zhonghuashishan
     */
    public Set smembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存成员是否存在
     *
     * @param key
     * @param member
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    public Boolean sismember(String key, String member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }

    /**
     * 缓存有序区间值
     *
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return java.util.Set
     * @author zhonghuashishan
     */
    public Set zrangeByScore(String key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
    }

    /**
     * 缓存有序区间值
     *
     * @param key
     * @param min
     * @param max
     * @return java.util.Set
     * @author zhonghuashishan
     */
    public Set zrangeByScore2(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 倒序返回zset区间值
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set zrevrange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 缓存倒序排列指定区间值
     *
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return java.util.Set
     * @author zhonghuashishan
     */
    public Set zrevrangeByScore(String key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
    }

    /**
     * 缓存有序存储
     *
     * @param key
     * @param member
     * @param score
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    public Boolean zadd(String key, String member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    /**
     * 缓存有序存储
     *
     * @param key
     * @param values
     * @return java.lang.Long
     * @author zhonghuashishan
     */
    public Long zremove(String key, String... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 缓存有序数量
     *
     * @param key
     * @return java.lang.Long
     * @author zhonghuashishan
     */
    public Long zcard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }


    /**
     * 判断hash key是否存在
     *
     * @param key
     * @return
     */
    public boolean hExists(String key) {
        return hGetAll(key).isEmpty();
    }

    /**
     * 判断hash field是否存在
     * @return
     */
    public boolean hFieldExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 获取hash变量中的键值对
     * 对应redis hgetall 命令
     *
     * @param key
     * @return
     */
    public Map<String, String> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取hash变量中的
     * @return
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }


    /**
     * hash变量中field对应的value自增
     * @param key
     * @param field
     * @param increment
     * @return
     */
    public Long hIncr(String key, String field, Integer increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }

    /**
     * hash变量field对应的value自增
     * @param key
     * @param field
     * @return
     */
    public Long hIncr(String key, String field) {
        return hIncr(key, field, 1);
    }

    /**
     * 获取hash变量中的field数量
     * 对应redis hlen 命令
     *
     * @param key
     * @return
     */
    public Long hLen(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 添加hash的value
     * @param key
     * @param field
     * @param value
     */
    public void hPut(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 以map集合的形式添加hash键值对
     *
     * @param key
     * @param map
     */
    public void hPutAll(String key, Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 删除某个field
     * @param key
     * @param field
     */
    public long hDel(String key, String field) {
        return redisTemplate.opsForHash().delete(key, field);
    }

    /**
     * 设置key过期时间
     * @param key
     * @param time
     * @param unit
     */
    public void expire(String key, long time, TimeUnit unit) {
        redisTemplate.expire(key, time, unit);
    }

    /**
     * 以list集合的形式添加数据
     *
     * @param key
     * @return
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 以list集合的形式添加数据
     *
     * @param key
     * @param values
     * @return
     */
    public Long lPushAll(String key, String... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 以list集合的形式添加数据
     *
     * @param key
     * @param values
     * @return
     */
    public Long lPushAll(String key, List<String> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 以list集合的形式添加数据
     *
     * @param key
     * @param values
     * @return
     */
    public Long rPushAll(String key, String... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 以list集合的形式添加数据
     *
     * @param key
     * @param values
     * @return
     */
    public Long rPushAll(String key, List<String> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 返回list集合下表区间的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 返回list集合的大小
     *
     * @param key
     * @return
     */
    public Long lsize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 设置缓存过期时间
     *
     * @param key
     * @return
     */
    public void expire(String key, long time) {
        this.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 执行lua脚本
     *
     * @param script
     * @param keys
     * @param args
     * @param <T>
     * @return
     */
    public <T> T execute(RedisScript<T> script, List<String> keys, String... args) {
        return (T) redisTemplate.execute(script, keys, args);
    }

    /**
     * 缓存存储并设置过期时间
     *
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     * @return void
     */
    public void setex(String key, String value, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 缓存增量
     *
     * @param key
     * @param increment
     * @return java.lang.Long
     */
    public Long increment(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }
    /**
     * 缓存减量
     *
     * @param key
     * @param decrement
     * @return java.lang.Long
     */
    public Long decrement(String key, long decrement) {
        return redisTemplate.opsForValue().decrement(key, decrement);
    }
    /**
     * 缓存失效时间
     *
     * @param key
     * @param timeUnit
     * @return java.lang.Long
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key
     * @param date
     * @return java.lang.Boolean
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }

    /**
     * 获取hash多field值
     * @param key
     * @param list
     * @return
     */
    public List<String> multiGet(String key, List<String> list) {
        return redisTemplate.opsForHash().multiGet(key, list);
    }

    /**
     * 新增hash值
     *
     * @param key
     * @param field
     * @param value
     * @return java.lang.Boolean
     */
    public Boolean putIfAbsent(String key, String field, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, field, value);
    }

}
