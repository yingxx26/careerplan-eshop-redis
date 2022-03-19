package com.ruyuan.careerplan.cookbook.service;

/**
 * 菜谱数据刷新服务
 *
 * @author zhonghuashishan
 */
public interface CookbookRefreshService {

    /**
     * 刷新首页菜谱feed缓存数据
     */
    void refreshHomeFeedCookbook();

    /**
     * 刷新首页菜谱视图缓存数据
     */
    void refreshHomeViewCookbook();

}
