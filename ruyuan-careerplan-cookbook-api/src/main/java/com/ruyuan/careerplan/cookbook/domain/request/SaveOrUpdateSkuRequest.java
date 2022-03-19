package com.ruyuan.careerplan.cookbook.domain.request;

import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 新增/修改商品请求入参
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveOrUpdateSkuRequest implements Serializable {

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
    private List<SkuInfoDTO.ImageInfo> skuImage;

    /**
     * 商品详情图
     * [{"sort":1, "img": "url"}]
     */
    private List<SkuInfoDTO.ImageInfo> detailImage;

    /**
     * 商品状态  1:上架  2:下架
     */
    private Integer skuStatus;

    private List<Inventory> inventories;

    /**
     * 操作人
     */
    private Integer operator;

    @Data
    public static class Inventory {
        /**
         * 库存数量
         */
        private Integer inventoryNum;
        /**
         * 仓库地址
         */
        private String warehouse;
    }

}