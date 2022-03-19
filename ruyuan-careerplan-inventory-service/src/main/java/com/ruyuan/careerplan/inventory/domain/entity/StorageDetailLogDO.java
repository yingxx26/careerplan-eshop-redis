package com.ruyuan.careerplan.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 出入库明细记录日志表
 * @author zhonghuashishan
 */
@Data
@TableName("storage_detail_log")
public class StorageDetailLogDO implements Serializable {

    /**
     * 出入库单号
     */
    private String storageCode;
    /**
     * 出入库时间
     */
    private Date storageTime;
    /**
     * 调出仓库
     */
    private String warehouse;
    /**
     * 商品编码
     */
    private Long skuId;
    /**
     * 调出数量
     */
    private Integer storageNum;
    /**
     * 出入库前数量
     */
    private Integer storageBeforeNum;
    /**
     * 出入库后数量
     */
    private Integer storageAfterNum;
    /**
     * 操作人
     */
    private Integer operator;
}
