package com.ruyuan.careerplan.goodscart.domain.dto;

import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSkuInfoDTO implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品编码
     */
    private Long skuId;

    /**
     * 商品名
     */
    private String title;

    /**
     * 图片url
     */
    private List<SkuInfoDTO.ImageInfo> image;

    /**
     * 选中状态
     */
    private Integer checkStatus;

    /**
     * 商品价格
     */
    private Integer price;

    /**
     * 商品数量
     */
    private Integer count;

    /**
     * 商品更新时间
     */
    private Date updateTime;

    /**
     * 卖家仓库地址
     */
    private String warehouse;
}
