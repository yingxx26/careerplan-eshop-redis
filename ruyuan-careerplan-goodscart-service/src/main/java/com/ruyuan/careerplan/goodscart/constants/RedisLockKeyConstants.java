package com.ruyuan.careerplan.goodscart.constants;

/**
 * 分布式锁KEY
 * @author zhonghuashishan
 */
public class RedisLockKeyConstants {

    /**
     * 购物车数据持久化
     */
    public static final String SHOPPING_CART_PERSISTENCE_KEY = "SHOPPING_CART_PERSISTENCE_KEY:";

    /**
     * 购物车数据修改锁
     */
    public static final String UPDATE_CART_LOCK_KEY = "UPDATE_CART_LOCK_KEY:";
}
