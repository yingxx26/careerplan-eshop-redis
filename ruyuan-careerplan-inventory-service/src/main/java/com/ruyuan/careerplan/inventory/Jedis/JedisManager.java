package com.ruyuan.careerplan.inventory.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
@Component
public class JedisManager implements DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisManager.class);

    private final List<JedisPool> jedisPools = new ArrayList<>();

    public JedisManager(JedisConfig jedisConfig) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(jedisConfig.getMaxTotal());
        jedisPoolConfig.setMaxIdle(jedisConfig.getMaxIdle());
        jedisPoolConfig.setMinIdle(jedisConfig.getMinIdle());

        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(false);

        for (String addr : jedisConfig.getRedisAddrs()) {
            String[] ipAndPort = addr.split(":");
            String redisIp = ipAndPort[0];
            int redisPort = Integer.parseInt(ipAndPort[1]);
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisIp, redisPort,3000,jedisConfig.getPassword());
            LOGGER.info("创建JedisPool, jedisPool={}", jedisPool);
            jedisPools.add(jedisPool);
        }
    }

    public int getRedisCount() {
        return jedisPools.size();
    }

    public Jedis getJedisByIndex(int index) {
        return jedisPools.get(index).getResource();
    }

    public Jedis getJedisByHashKey(long hashKey) {
        hashKey = Math.abs(hashKey);
        int index = (int)(hashKey % getRedisCount());
        return getJedisByIndex(index);
    }

    public Jedis getJedisByHashKey(int hashKey) {
        hashKey = Math.abs(hashKey);
        int index = hashKey % getRedisCount();
        return getJedisByIndex(index);
    }

    @Override
    public void destroy() throws Exception {
        for (JedisPool jedisPool : jedisPools) {
            LOGGER.info("关闭jedisPool, jedisPool={}", jedisPool);
            jedisPool.close();
        }
    }
}