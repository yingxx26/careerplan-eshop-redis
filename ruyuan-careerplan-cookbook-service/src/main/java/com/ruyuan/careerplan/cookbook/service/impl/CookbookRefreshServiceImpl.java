package com.ruyuan.careerplan.cookbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.ruyuan.careerplan.common.enums.DeleteStatusEnum;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.cookbook.constants.HomeConstant;
import com.ruyuan.careerplan.cookbook.dao.CookbookDAO;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookDO;
import com.ruyuan.careerplan.cookbook.service.CookbookRefreshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜谱数据刷新服务
 *
 * @author zhonghuashishan
 */
@Service
@Slf4j
public class CookbookRefreshServiceImpl implements CookbookRefreshService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private CookbookDAO cookbookDAO;

    /**
     * 分页查询
     */
    private static final int pageNo = 1;
    private static final int pageSize = 1000;

    /**
     * 数字值
     */
    private static final int INTEGER_90 = 90;
    private static final int INTEGER_200 = 200;
    private static final int INTEGER_2000 = 2000;



    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());

    /**
     * 刷新首页菜谱feed缓存数据
     * 只处理最多1000条数据，没有必要将所有数据都放入缓存
     */
    @Override
    public void refreshHomeFeedCookbook() {
        LambdaQueryWrapper<CookbookDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CookbookDO::getCookbookStatus, DeleteStatusEnum.NO.getCode());
        List<CookbookDTO> cookbookDTOList = cookbookDAO.pageByCookbookStatus(DeleteStatusEnum.NO.getCode(), pageNo, pageSize);
        if(CollectionUtils.isEmpty(cookbookDTOList)) {
            return;
        }

        String feedVersion = String.valueOf(System.currentTimeMillis());
        List<Long> cookbookIdList = cookbookDTOList.stream().map(CookbookDTO::getId).collect(Collectors.toList());

        // feed流中存储的菜谱ID集合
        redisCache.rPushAll(String.format(HomeConstant.HOME_FEED_KEY, feedVersion), cookbookIdList.stream().map(Object::toString).toArray(String[]::new));

        // 首页feed流最新版本号
        redisCache.set(HomeConstant.HOME_FEED_LATEST_VERSION_KEY, feedVersion, 0);
    }

    /**
     * 刷新首页菜谱视图缓存数据
     * 只处理最多1000条数据，没有必要将所有数据都放入缓存
     */
    @Override
    public void refreshHomeViewCookbook() {
        LambdaQueryWrapper<CookbookDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CookbookDO::getCookbookStatus, DeleteStatusEnum.NO.getCode());
        List<CookbookDTO> cookbookDTOList = cookbookDAO.pageByCookbookStatus(DeleteStatusEnum.NO.getCode(), pageNo, pageSize);
        if(CollectionUtils.isEmpty(cookbookDTOList)) {
            return;
        }

        List<List<CookbookDTO>> partionList = Lists.partition(cookbookDTOList, INTEGER_200);
        for (List<CookbookDTO> cookbookList : partionList) {
            executor.submit(() -> {
                try {
                    extracted(cookbookList);
                } catch (Exception e) {
                    log.error("刷新首页菜谱视图缓存数据异常", e);
                }
            });
            int activeCount = executor.getActiveCount();
            if (activeCount > INTEGER_90) {
                log.info("线程池快满了，休息处理，activeCount={}", activeCount);
                try {
                    Thread.sleep(INTEGER_2000);
                } catch (InterruptedException e) {
                    log.error("线程池快满了，休息处理 异常", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 刷新首页菜谱视图缓存数据
     *
     * @param cookbookList
     * @return void
     */
    private void extracted(List<CookbookDTO> cookbookList) {
        for (CookbookDTO cookbook : cookbookList) {
            // 菜谱视图缓存
            redisCache.putIfAbsent(HomeConstant.COOKBOOK_VIEW_KEY, String.valueOf(cookbook.getId()), JSON.toJSONString(cookbook));
        }
    }

}
