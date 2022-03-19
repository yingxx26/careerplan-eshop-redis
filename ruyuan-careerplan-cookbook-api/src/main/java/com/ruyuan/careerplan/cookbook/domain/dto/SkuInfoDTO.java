package com.ruyuan.careerplan.cookbook.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 商品信息
 *
 * @author zhonghuashishan
 */
@Data
public class SkuInfoDTO implements Serializable {
    /**
     * 商品编码
     */
    private Long skuId;

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
    private List<ImageInfo> skuImage;

    /**
     * 商品详情图
     * [{"sort":1, "img": "url"}]
     */
    private List<ImageInfo> detailImage;

    /**
     * 商品状态  1:上架  2:下架
     */
    private Integer skuStatus;

    @Data
    public static class ImageInfo implements Serializable{

        /**
         * 排序字段，从小到大
         */
        private Integer sort;

        /**
         * 图片url
         */
        private String img;
    }

    private Date updateTime;
}