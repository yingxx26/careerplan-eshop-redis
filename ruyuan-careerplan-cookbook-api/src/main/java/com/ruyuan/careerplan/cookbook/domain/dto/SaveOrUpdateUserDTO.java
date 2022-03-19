package com.ruyuan.careerplan.cookbook.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增/修改作者返回结果
 *
 * @author zhonghuashishan
 */
@Data
@Builder
public class SaveOrUpdateUserDTO implements Serializable {

    /**
     * 操作成功
     */
    private Boolean success;

    /**
     * 作者id
     */
    private Long userId;
}