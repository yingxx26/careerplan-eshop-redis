package com.ruyuan.careerplan.goodscart.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhonghuashishan
 */
@Data
@TableName("cookbook_coupon_receive")
public class CookBookCouponReceiveDO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券状态 0 未使用，1：已使用，-1 已过期
     */
    private Integer status;

    private Long createUser;

    private Date createTime;

    private Long updateUser;

    private Date updateTime;

    private Integer deleteFlag;
}
