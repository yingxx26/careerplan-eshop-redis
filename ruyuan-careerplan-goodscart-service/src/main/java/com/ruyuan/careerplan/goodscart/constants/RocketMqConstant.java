package com.ruyuan.careerplan.goodscart.constants;

/**
 * RocketMQ 常量类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public class RocketMqConstant {

    /**
     * 购物车的商品落库TOPIC
     */
    public static final String COOKBOOK_ASYNC_PERSISTENCE_MESSAGE_SEND_TOPIC = "cookbook_async_persistence_message_send_topic";

    /**
     * 购物车商品落库TOPIC group
     */
    public static final String COOKBOOK_ASYNC_PERSISTENCE_MESSAGE_SEND_GROUP = "cookbook_async_persistence_message_send_group";

    /**
     * 购物车的商品更新TOPIC
     */
    public static final String COOKBOOK_ASYNC_UPDATE_MESSAGE_SEND_TOPIC = "cookbook_async_update_message_send_topic";

    /**
     * 购物车的商品更新TOPIC group
     */
    public static final String COOKBOOK_ASYNC_UPDATE_MESSAGE_SEND_GROUP = "cookbook_async_update_message_send_group";

    /**
     * 购物车的商品降价通知TOPIC
     */
    public static final String COOKBOOK_SHOPPING_CART_SALE_MESSAGE_SEND_TOPIC = "cookbook_shopping_cart_sale_message_send_topic";

    /**
     * 默认的producer分组
     */
    public static final String PUSH_DEFAULT_PRODUCER_GROUP = "cookbook_push_default_producer_group";

    /**
     * 购物车选中状态TOPIC
     */
    public static final String COOKBOOK_ASYNC_CHECKED_STATUS_MESSAGE_SEND_TOPIC = "cookbook_async_checked_status_message_send_topic";

    /**
     * 购物车选中状态TOPIC group
     */
    public static final String COOKBOOK_ASYNC_CHECKED_STATUS_MESSAGE_SEND_GROUP = "cookbook_async_checked_status_message_send_group";
}
