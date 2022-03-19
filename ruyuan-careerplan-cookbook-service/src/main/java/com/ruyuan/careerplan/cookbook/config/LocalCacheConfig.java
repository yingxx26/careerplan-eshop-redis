package com.ruyuan.careerplan.cookbook.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置
 *
 * @author zhonghuashishan
 */
@Configuration
public class LocalCacheConfig {

    @Value("${localCache.expireSeconds}")
    private Long expireSeconds;

    @Value("${localCache.initialCapacity}")
    private Integer initialCapacity;

    @Value("${localCache.maxSize}")
    private Long maxSize;

    @Bean
    public Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterAccess(expireSeconds, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(initialCapacity)
                // 缓存的最大条数
                .maximumSize(maxSize)
                .build();
    }
}