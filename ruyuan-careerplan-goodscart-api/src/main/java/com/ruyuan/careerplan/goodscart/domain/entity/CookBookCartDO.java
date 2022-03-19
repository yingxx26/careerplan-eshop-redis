package com.ruyuan.careerplan.goodscart.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhonghuashishan
 */
@Data
@TableName("cookbook_cart")
public class CookBookCartDO implements Serializable {
    private Long id;

    private Long userId;

    private Long skuId;

    private Integer checkStatus;

    private Integer count;

    private Integer addAmount;

    private Long createUser;

    private Date createTime;

    private Long updateUser;

    private Date updateTime;
}
