package com.ruyuan.careerplan.goodscart.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhonghuashishan
 */
@Data
@TableName("cookbook_coupon")
public class CookBookCouponDO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券金额
     */
    private Integer couponAmount;

    /**
     * 门槛（除无门槛卷外，其他劵应满足此条件才能优惠）
     */
    private Integer threshold;

    /**
     * 生效时间
     */
    private Date startTime;

    /**
     * 失效时间
     */
    private Date endTime;

    /**
     * 状态 0-生效；1-失效；2-过期
     */
    private Integer status;

    /**
     * 优惠券说明
     */
    private String remark;

    private Long createUser;

    private Date createTime;

    private Long updateUser;

    private Date updateTime;

    private Integer deleteFlag;
}
