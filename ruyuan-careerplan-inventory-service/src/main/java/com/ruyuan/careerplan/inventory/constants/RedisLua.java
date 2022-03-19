package com.ruyuan.careerplan.inventory.constants;
/**
 *  lua脚本
 *  @author zhonghuashishan
 */
public class RedisLua {

    /**
     * 扣减库存
     */
    public static final String SCRIPT  =
            "if (redis.call('exists', KEYS[1]) == 1) then"
            + "    local stock = tonumber(redis.call('get', KEYS[1]));"
            + "    local num = tonumber(ARGV[1]);"
            + "    local results_num = stock - num"
            + "    if (results_num <= 0) then"
            + "        return -1;"
            + "    end;"
            + "    if (stock >= num) then"
            + "            return redis.call('incrBy', KEYS[1], 0 - num);"
            + "        end;"
            + "    return -2;"
            + "end;"
            + "return -3;";
    /**
     * 合并库存扣减
     */
    public static final String MERGE_SCRIPT  =
            "if (redis.call('exists', KEYS[1]) == 1) then\n" +
                    "    local stock = tonumber(redis.call('get', KEYS[1]));\n" +
                    "    local num = tonumber(ARGV[1]);\n" +
                    "    local diff_num = stock - num;\n" +
                    "    if (stock <= 0) then\n" +
                    "        return -1;\n" +
                    "    end;\n" +
                    "    if (num > stock) then\n" +
                    "        num = stock;\n" +
                    "    end;\n" +
                    "    redis.call('incrBy', KEYS[1], 0 - num);\n" +
                    "    if (diff_num < 0) then\n" +
                    "        return 0-diff_num;\n" +
                    "    end;\n" +
                    "    return 0;\n" +
                    "end;\n" +
                    "return -3;";

    /**
     * 初始化新增库存
     */
    public static final String ADD_INVENTORY =
            "if (redis.call('exists', KEYS[1]) == 1) then"
                    + "    local occStock = tonumber(redis.call('get', KEYS[1]));"
                    + "    if (occStock >= 0) then"
                    + "        return redis.call('incrBy', KEYS[1], ARGV[1]);"
                    + "    end;"
                    + "end;"
                    + "       redis.call('SET', KEYS[1], ARGV[1]);"
                    + "    return tonumber(redis.call('get', KEYS[1]));";


    /**
     * 查询库存
     */
    public static final String QUERY_STOCK =
                    "    local occStock = tonumber(redis.call('get', KEYS[1]));"
                    + "    if (occStock == nil) then"
                    + "        return 0;"
                    + "    end;"
                    + "    return occStock;";



}
