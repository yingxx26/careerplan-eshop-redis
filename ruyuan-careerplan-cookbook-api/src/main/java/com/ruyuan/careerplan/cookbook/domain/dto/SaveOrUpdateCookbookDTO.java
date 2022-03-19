package com.ruyuan.careerplan.cookbook.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 新增/修改菜谱返回结果
 *
 * @author zhonghuashishan
 */
@Data
@Builder
public class SaveOrUpdateCookbookDTO implements Serializable {

    /**
     * 是否保存成功
     */
    private Boolean success;

    /**
     * 菜谱id
     */
    private Long cookbookId;

}