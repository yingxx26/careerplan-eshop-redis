package com.ruyuan.careerplan.goodscart.constants;

/**
 * @author zhonghuashishan
 */
public class RedisKeyConstant {
    /**
     * 购物车sku数量hash key
     */
    public static final String SHOPPING_CART_HASH = "shopping_cart_hash:";

    /**
     * 购物车商品空缓存 key
     */
    public static final String SHOPPING_CART_EMPTY = "shopping_cart_empty:";

    /**
     * 购物车sku扩展信息hash key
     */
    public static final String SHOPPING_CART_EXTRA_HASH = "shopping_cart_extra_hash:";

    /**
     * 购物车sku操作时间戳 zset key
     */
    public static final String SHOPPING_CART_ZSET = "shopping_cart_zset:";
}
