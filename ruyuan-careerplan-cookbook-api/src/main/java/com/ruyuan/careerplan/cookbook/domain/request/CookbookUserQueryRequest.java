package com.ruyuan.careerplan.cookbook.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询菜谱作者信息请求入参
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookbookUserQueryRequest implements Serializable {

    /**
     * 作者id
     */
    private Long userId;
}