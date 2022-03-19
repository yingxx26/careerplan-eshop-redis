package com.ruyuan.careerplan.cookbook.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询菜谱信息请求入参
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookbookQueryRequest implements Serializable {

    /**
     * 菜谱id
     */
    private Long cookbookId;

    /**
     * 菜谱作者
     */
    private Long userId;

    /**
     * 页码
     */
    private Integer pageNo = 1;
    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}