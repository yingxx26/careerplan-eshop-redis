package com.ruyuan.careerplan.home.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜谱视图请求入参
 */
@Data
public class CookbookViewRequestDTO implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * feed流请求版本号
     */
    private String feedVersion;

    /**
     * 是否下拉刷新
     */
    private Boolean pullToRefresh;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页数量
     */
    private Integer pageSize;

}
