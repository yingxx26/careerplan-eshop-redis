package com.ruyuan.careerplan.monitor.constants;

/**
 * RocketMQ 常量类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public class RocketMqConstant {

    /**
     * Redis大key推送binlog TOPIC
     */
    public static final String BINLOG_MONITOR_LARGE_KEY_TOPIC = "binlog_monitor_large_key_topic";

    /**
     * Redis大key推送binlog分组
     */
    public static final String BINLOG_MONITOR_LARGE_KEY_GROUP = "binlog_monitor_large_key_group";

    /**
     * 默认的producer分组
     */
    public static final String PUSH_DEFAULT_PRODUCER_GROUP = "cookbook_push_default_producer_group";

    /**
     * redis淘汰key推送分组
     */
    public static final String EVICTED_MONITOR_KEY_GROUP = "evicted_monitor_key_group";

    /**
     * redis淘汰key推送topic
     */
    public static final String EVICTED_MONITOR_KEY_TOPIC = "evicted_monitor_key_topic";
}
