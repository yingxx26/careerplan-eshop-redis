package com.ruyuan.careerplan.inventory.constants;

/**
 * RocketMQ 常量类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public class RocketMqConstant {

    /**
     * 库存变更落库TOPIC
     */
    public static final String INVENTORY_PRODUCT_STOCK_TOPIC = "inventory_product_stock_topic";
    /**
     * 库存补偿缓存TOPIC
     */
    public static final String COMPENSATION_PRODUCT_STOCK_TOPIC = "compensation_product_stock_topic";
    /**
     * 默认的producer分组
     */
    public static final String PUSH_DEFAULT_PRODUCER_GROUP = "inventory_push_default_producer_group";
    /**
     * 补偿缓存的producer分组
     */
    public static final String PUSH_COMPENSATION_PRODUCER_GROUP = "compensation_push_producer_group";

}
