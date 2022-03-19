package com.ruyuan.careerplan.home.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐菜谱
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendCookbookDTO {

    /**
     * 菜谱ID集合
     */
    private List<String> cookbookIdList;

    /**
     * 是否有下一页
     */
    private Boolean hasNextPage;

}
