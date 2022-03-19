package com.ruyuan.careerplan.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 出入库存储信息
 * @author zhonghuashishan
 */
@Data
@TableName("storage_info")
public class StorageInfoDO implements Serializable {

    /**
     * 出入库单号
     */
    private String storageCode;
    /**
     * 出入库时间
     */
    private Date storageTime;
    /**
     * 存储仓库编码
     */
    private String warehouse;
    /**
     * 商品SKU
     */
    private Long skuId;

    /**
     * 调出数量
     */
    private Integer storageNum;
    /**
     * 操作人
     */
    private Integer operator;
}
