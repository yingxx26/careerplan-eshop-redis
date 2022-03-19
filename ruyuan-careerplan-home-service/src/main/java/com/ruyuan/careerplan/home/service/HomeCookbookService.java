package com.ruyuan.careerplan.home.service;

import com.ruyuan.careerplan.home.domain.dto.CookbookViewDTO;

import java.util.List;

/**
 * 首页菜谱服务
 */
public interface HomeCookbookService {

    /**
     * 获取首页feed流版本号
     *
     * @return java.lang.String
     */
    String getHomeCookLatestVersion();

    /**
     * 是否存在首页feed流缓存
     *
     * @param feedVersion
     * @return java.lang.Boolean
     */
    Boolean isExistHomeFeedCache(String feedVersion);

    /**
     * 获取分页后的首页feed流缓存
     *
     * @param version
     * @param pageNo
     * @return java.util.List<java.lang.String>
     */
    List<String> getHomeFeedCookbookList(String version, Integer pageNo);

    /**
     * 获取首页feed流菜谱列表大小
     *
     * @param version
     * @return java.lang.Long
     */
    Long getHomeFeedCookbookListSize(String version);


    /**
     * 批量查询菜谱的视图缓存信息
     *
     * @param cookbookIdList
     * @return java.util.List<com.ruyuan.careerplan.home.domain.dto.CookbookViewDTO>
     */
    List<CookbookViewDTO> getCookbookViewFromCache(List<String> cookbookIdList);

    /**
     * 批量获取菜谱视图信息
     *
     * @param cookbookIdList
     * @return java.util.List
     */
    List batchCookbookViewDetail(List<String> cookbookIdList);

}
