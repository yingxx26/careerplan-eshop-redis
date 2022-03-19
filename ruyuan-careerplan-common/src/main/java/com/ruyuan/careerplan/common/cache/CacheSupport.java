package com.ruyuan.careerplan.common.cache;

import com.ruyuan.careerplan.common.utils.RandomUtil;

/**
 * @author zhonghuashishan
 */
public interface CacheSupport {

    /**
     * 缓存空数据
     */
    String EMPTY_CACHE = "{}";

    Integer TWO_DAYS_SECONDS = 2 * 24 * 60 * 60;

    Integer ONE_HOURS_SECONDS = 1 * 60 * 60;

    /**
     * 生成缓存穿透过期时间，单位 秒
     *
     * @return
     */
    static Integer generateCachePenetrationExpireSecond() {
        return RandomUtil.genRandomInt(30, 100);
    }


    /**
     * 生成缓存过期时间
     * 2天加上随机几小时
     *
     * @return
     */
    static Integer generateCacheExpireSecond() {
        return TWO_DAYS_SECONDS + RandomUtil.genRandomInt(0, 10) * 60 * 60;
    }

}
