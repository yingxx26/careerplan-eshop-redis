package com.ruyuan.careerplan.inventory.constants;

/**
 * redis key 常量
 *
 * @author zhonghuashishan
 */
public class RedisKeyConstants {
    /**
     *  同步库存均匀的key
     */
    public static final String PRODUCT_STOCK_UNIFORM = "product_stock_uniform";

    /**
     * 商品库存入库锁的前缀
     */
    public static final String INVENTORY_LOCK_PREFIX = "inventory_lock:";

    /**
     * 某个商品被请求扣减的次数
     */
    public static final String PRODUCT_STOCK_COUNT_PREFIX = "product_stock_count:";

    /**
     * 商品库存分桶的前缀
     */
    public static final String PRODUCT_STOCK_PREFIX = "product_stock:";
}
