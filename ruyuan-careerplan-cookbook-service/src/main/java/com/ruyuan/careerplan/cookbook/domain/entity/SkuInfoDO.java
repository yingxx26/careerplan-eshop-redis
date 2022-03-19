package com.ruyuan.careerplan.cookbook.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ruyuan.careerplan.common.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;


/**
 * @author zhonghuashishan
 */
@Data
@TableName("sku_info")
public class SkuInfoDO extends BaseEntity implements Serializable {

    /**
     * 商品名称
     */
    private String skuName;

    /**
     * 价格（单位为分）
     */
    private Integer price;

    /**
     * 会员价（单位为分）
     */
    private Integer vipPrice;

    /**
     * 主图链接
     */
    private String mainUrl;

    /**
     * 商品轮播图
     * [{"sort":1, "img": "url"}]
     */
    private String skuImage;

    /**
     * 商品详情图
     * [{"sort":1, "img": "url"}]
     */
    private String detailImage;

    /**
     * 商品状态  1:上架  2:下架
     */
    private Integer skuStatus;

    /**
     * 创建人
     */
    private Integer createUser;

    /**
     * 修改人
     */
    private Integer updateUser;
}