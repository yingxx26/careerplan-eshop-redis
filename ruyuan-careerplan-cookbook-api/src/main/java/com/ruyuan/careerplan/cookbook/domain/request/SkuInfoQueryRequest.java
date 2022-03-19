package com.ruyuan.careerplan.cookbook.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 商品查询请求入参
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuInfoQueryRequest implements Serializable {

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 商品id list查询
     */
    private List<Long> skuIds;

}