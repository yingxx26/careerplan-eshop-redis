package com.ruyuan.careerplan.inventory.Jedis;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
public class RedisCacheSupport implements CacheSupport {

    private final JedisManager jedisManager;

    public RedisCacheSupport(JedisManager jedisManager) {
        this.jedisManager = jedisManager;
    }

    @Override
    public int getRedisCount() {
        return jedisManager.getRedisCount();
    }

    @Override
    public Boolean exists(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())){
            return jedis.exists(key);
        }
    }

    @Override
    public Long expire(String key, int seconds) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())){
            return jedis.expire(key, seconds);
        }
    }

    @Override
    public Long del(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())){
            return jedis.del(key);
        }
    }

    @Override
    public Long delOnAllRedis(String key) {
        for (int i = 0; i < jedisManager.getRedisCount(); i++) {
            try (Jedis jedis = jedisManager.getJedisByIndex(i)) {
                jedis.del(key);
            }
        }
        return 1L;
    }

    @Override
    public String set(String key, String value) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())){
            return jedis.set(key, value);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())){
            return jedis.get(key);
        }
    }

    @Override
    public Long incr(String key) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(key.hashCode())){
            return jedis.incr(key);
        }
    }

    @Override
    public Object eval(Long hashKey, String script) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(hashKey)){
            return jedis.eval(script);
        }
    }
    @Override
    public Object eval(Long hashKey, String script,List<String> keys, List<String> args) {
        try (Jedis jedis = jedisManager.getJedisByHashKey(hashKey)){
            return jedis.eval(script,keys,args);
        }
    }

    @Override
    public List<Map<String, String>> hgetAllOnAllRedis(String key) {
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < jedisManager.getRedisCount(); i++) {
            try (Jedis jedis = jedisManager.getJedisByIndex(i)) {
                 list.add(jedis.hgetAll(key));
            }
        }
        return list;
    }

    @Override
    public void hsetOnAllRedis(String key, List<Map<String, String>> hashList) {
        for (int i = 0; i < jedisManager.getRedisCount(); i++) {
            try (Jedis jedis = jedisManager.getJedisByIndex(i)) {
                jedis.hset(key, hashList.get(i));
            }
        }
    }
}
