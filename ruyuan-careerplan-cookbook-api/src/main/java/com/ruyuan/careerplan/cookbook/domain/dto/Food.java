package com.ruyuan.careerplan.cookbook.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    /**
     * 排序，从小到大排
     */
    private Integer sort;

    /**
     * 食材名称
     */
    private String foodName;

    /**
     * 食材标签
     */
    private String tag;

    /**
     * 食材规格
     */
    private String foodSpecs;
}

