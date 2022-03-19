package com.ruyuan.careerplan.inventory.service;

import com.ruyuan.careerplan.inventory.InventoryApplication;
import com.ruyuan.careerplan.inventory.constants.RedisKeyConstants;
import org.springframework.boot.SpringApplication;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {

    public static void main(String[] args)throws Exception {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(1);

        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(false);
        String productStockKey = RedisKeyConstants.PRODUCT_STOCK_PREFIX  +"10000001";
         String addr="127.0.0.1";
         for (int i=0;i<=2;i++){
             int redisPort = 6380;
             JedisPool jedisPool = new JedisPool(jedisPoolConfig, addr, redisPort+i,3000,"root");
             Jedis jedis = jedisPool.getResource();
             System.out.println(jedis.get(productStockKey));
         }



    }
}
