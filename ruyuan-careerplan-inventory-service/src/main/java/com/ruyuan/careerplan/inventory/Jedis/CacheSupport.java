package com.ruyuan.careerplan.inventory.Jedis;

import java.util.List;
import java.util.Map;

public interface CacheSupport {

    //-------------------------------------------其他
    int getRedisCount();

    //-------------------------------------------key操作
    Boolean exists(String key);

    Long expire(String key, int seconds);

    Long del(String key);

    Long delOnAllRedis(String key);

    //-------------------------------------------string操作
    String set(String key, String value);

    String get(String key);

    Long incr(String key);

    //-------------------------------------------hash操作
    void hsetOnAllRedis(String key, List<Map<String, String>> hashList);

    List<Map<String, String>> hgetAllOnAllRedis(String key);

    //-------------------------------------------lua操作
    Object eval(Long hashKey, String script);

    Object eval(Long hashKey, String script,List<String> keys, List<String> args);
}