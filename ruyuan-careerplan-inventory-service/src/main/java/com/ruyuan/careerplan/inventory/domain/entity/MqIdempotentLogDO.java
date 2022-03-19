package com.ruyuan.careerplan.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
/**
 * 消息幂等表
 * @author zhonghuashishan
 */
@Data
@TableName("mq_idempotent_log")
public class MqIdempotentLogDO implements Serializable {
    /**
     * 主键
     */
    private Long id;
    /**
     * 消息ID
     */
    private String msgId;
}
