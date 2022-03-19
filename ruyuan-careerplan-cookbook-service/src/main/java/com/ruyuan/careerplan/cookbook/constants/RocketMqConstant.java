package com.ruyuan.careerplan.cookbook.constants;

/**
 * RocketMQ 常量类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public class RocketMqConstant {
    /**
     * 菜谱服务producer group
     */
    public static final String COOKBOOK_DEFAULT_PRODUCER_GROUP = "cookbook_default_producer_group";

    /**
     * 菜谱服务consumer group
     */
    public static final String COOKBOOK_DEFAULT_CONSUMER_GROUP = "cookbook_default_consumer_group";


    /**
     * 菜谱新增/修改 topic
     */
    public static final String COOKBOOK_UPDATE_MESSAGE_TOPIC = "cookbook_update_message_topic";

    /**
     * binlog监听变化 group
     */
    public static final String BINLOG_DEFAULT_CONSUMER_GROUP = "binlog_default_consumer_group";


    /**
     * binlog 监听变化topic
     */
    public static final String BINLOG_DEFAULT_CONSUMER_TOPIC = "binlog_default_consumer_topic";

}
