package com.ruyuan.careerplan.home.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜谱视图结果
 *
 * @author zhonghuashishan
 */
@Data
public class CookbookViewResultDTO implements Serializable {

    /**
     * feed流请求版本号
     */
    private String feedVersion;

    /**
     * 当前页
     */
    private int currentPageNo;

    /**
     * 是否有下一页
     */
    private Boolean hasNextPage;

    /**
     * 菜谱视图列表
     */
    private List<CookbookViewDTO> cookbookViewDTOList;

}
