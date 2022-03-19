package com.ruyuan.careerplan.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存表
 * @author zhonghuashishan
 */
@Data
@TableName("inventory_info")
public class InventoryDO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 商品SKU
     */
    private Long skuId;
    /**
     * 仓库编码
     */
    private String warehouse;
    /**
     * 库存数量
     */
    private Integer inventoryNum;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 操作人
     */
    private Integer operator;
}
