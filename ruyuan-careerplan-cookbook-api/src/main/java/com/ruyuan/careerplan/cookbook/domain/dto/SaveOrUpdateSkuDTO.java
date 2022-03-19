package com.ruyuan.careerplan.cookbook.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 新增/修改商品返回结果
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveOrUpdateSkuDTO implements Serializable {

    /**
     * 是否操作成功
     */
    private Boolean success;

    /**
     * 商品编码
     */
    private Long skuId;

}