package com.ruyuan.careerplan.goodscart.constants;

/**
 * 相关的常量
 *
 * @author zhonghuashishan
 */
public class CookbookCartConstants {

    /**
     * 购物车加购默认数量
     */
    public static final Integer ADD_CART_DEFAULT_SKU_COUNT = 1;

    /**
     * 用于获取zset所有数据的索引，开始位置索引
     */
    public static final Integer ZSET_ALL_RANGE_START_INDEX = 0;

    /**
     * zset结束位置索引
     */
    public static final Integer ZSET_ALL_RANGE_END_INDEX = -1;

    /**
     * 最大的默认购物车sku数量
     */
    public static final Integer CART_DEFAULT_MAX_SKU_COUNT = 100;

    /**
     * 未使用的优惠券状态
     */
    public static final Integer UNUSED_COUPON_STATUS = 0;

    /**
     * 生效的优惠券状态
     */
    public static final Integer AVAILABLE_COUPON_STATUS = 0;

    /**
     * redis空key过期时间
     */
    public static final Long EXPIRE_TIME = 30L;

    /**
     * redis空key标识
     */
    public static final String EMPTY_CACHE_IDENTIFY = "$";
}
