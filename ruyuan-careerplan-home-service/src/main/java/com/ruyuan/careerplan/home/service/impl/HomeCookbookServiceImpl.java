package com.ruyuan.careerplan.home.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.utils.PageUtil;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.home.constants.HomeConstant;
import com.ruyuan.careerplan.home.domain.dto.CookbookViewDTO;
import com.ruyuan.careerplan.home.service.HomeCookbookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页菜谱服务
 */
@Slf4j
@Service
public class HomeCookbookServiceImpl implements HomeCookbookService {

    @Resource
    private RedisCache redisCache;

    /**
     * 获取首页feed流版本号
     *
     * @return java.lang.String
     */
    @Override
    public String getHomeCookLatestVersion() {
        return redisCache.get(HomeConstant.HOME_FEED_LATEST_VERSION_KEY);
    }

    /**
     * 是否存在首页feed流缓存
     *
     * @param feedVersion
     * @return java.lang.Boolean
     */
    @Override
    public Boolean isExistHomeFeedCache(String feedVersion) {
        if (StringUtils.isBlank(feedVersion)) {
            return false;
        }
        return redisCache.hasKey(String.format(HomeConstant.HOME_FEED_KEY, feedVersion));
    }

    /**
     * 获取分页后的首页feed流缓存
     *
     * @param feedVersion
     * @param pageNo
     * @return java.util.List<java.lang.String>
     */
    @Override
    public List<String> getHomeFeedCookbookList(String feedVersion, Integer pageNo) {
        if (StringUtils.isBlank(feedVersion)) {
            return Lists.newArrayList();
        }
        if (pageNo < HomeConstant.PAGE_NO_START) {
            pageNo = HomeConstant.PAGE_NO_START;
        }
        String key = String.format(HomeConstant.HOME_FEED_KEY, feedVersion);
        int start = PageUtil.pageStart(pageNo, HomeConstant.DEFAULT_PAGE_SIZE);
        int end = PageUtil.redisPageEnd(pageNo, HomeConstant.DEFAULT_PAGE_SIZE);
        List<String> cookIdList = redisCache.lRange(key, start, end);
        if (CollectionUtils.isEmpty(cookIdList)) {
            return Lists.newArrayList();
        }
        return cookIdList;
    }

    /**
     * 获取首页feed流菜谱列表大小
     *
     * @param feedVersion
     * @return java.lang.Long
     */
    @Override
    public Long getHomeFeedCookbookListSize(String feedVersion) {
        if (StringUtils.isBlank(feedVersion)) {
            return 0L;
        }
        String key = String.format(HomeConstant.HOME_FEED_KEY, feedVersion);
        return redisCache.lsize(key);
    }

    /**
     * 批量查询菜谱的视图缓存信息
     *
     * @param cookbookIdList
     * @return java.util.List<com.ruyuan.careerplan.home.domain.dto.CookbookViewDTO>
     */
    @Override
    public List<CookbookViewDTO> getCookbookViewFromCache(List<String> cookbookIdList) {
        List<CookbookViewDTO> cookbookViewDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cookbookIdList)) {
            return cookbookViewDTOList;
        }
        List<String> jsonList = batchCookbookViewDetail(cookbookIdList);
        if (CollectionUtils.isEmpty(jsonList)) {
            return cookbookViewDTOList;
        }

        for (String json : jsonList) {
            if (StringUtils.isBlank(json)) {
                continue;
            }

            CookbookDTO cookbookDTO = JSON.parseObject(json, CookbookDTO.class);
            if (cookbookDTO != null) {
                CookbookViewDTO cookbookViewDTO = new CookbookViewDTO();
                BeanUtils.copyProperties(cookbookDTO, cookbookViewDTO);
                cookbookViewDTOList.add(cookbookViewDTO);
            }
        }

        return cookbookViewDTOList;
    }

    /**
     * 批量获取菜谱视图信息
     *
     * @param cookbookIdList
     * @return java.util.List
     */
    @Override
    public List batchCookbookViewDetail(List<String> cookbookIdList) {
        return redisCache.multiGet(HomeConstant.COOKBOOK_VIEW_KEY, cookbookIdList);
    }

}
