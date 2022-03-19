package com.ruyuan.careerplan.cookbook.limiter;

import com.google.common.util.concurrent.RateLimiter;
import com.ruyuan.careerplan.cookbook.constants.CookbookConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Data
@Component
public class LimiterProperties {

    /**
     * 如果需要替换配置文件信息，将配置文件放在这里就可以
     */
    @Value("${limiter.propertiesPath}")
    private String limiterPropertiesPath;

    /**
     * 初始化时候使用的配置信息
     */
    @Value(value = "classpath:/limiter.properties")
    private Resource limiterProperties;

    /**
     * 默认的redis宕机之后的限流器
     */
    @Value("${limiter.noRedisLimiter}")
    private Double noRedisLimiter;


    /**
     * 默认的限流器
     */
    @Value("${limiter.defaultLimiter}")
    private Double defaultLimiter;

    /**
     * 限流器配置信息
     */
    private Properties properties;

    /**
     * 各个接口的限流，每个接口限流都可以单独配置
     */
    private Map<String, RateLimiter> apiRateLimiterMap = null;

    /**
     * redis挂掉之后限流
     */
    private RateLimiter noRedisRateLimiter = null;


    @PostConstruct
    private void initLimiter() {
        // 初始化限流配置。
        properties = new Properties();
        try {
            properties.load(limiterProperties.getInputStream());
        } catch (IOException e) {
            log.error("读取配置文件失败", e);
        }
        this.reloadLimiter();
    }

    private void reloadLimiter() {
        /*
         * 加载没有redis时候的限流器
         * 优先读取限流配置文件中的配置，如果没有，则使用项目配置文件中的配置
         */
        String noRedisLimiterProperty = properties.getProperty(CookbookConstants.NO_REDIS_LIMITER);
        noRedisRateLimiter = RateLimiter.create(
                StringUtils.isEmpty(noRedisLimiterProperty) ? noRedisLimiter : Double.valueOf(noRedisLimiterProperty));

        /*
         * 加载配置文件中配置的接口对应的限流器
         */
        apiRateLimiterMap = new HashMap<>(16);
        // 优先从配置文件中读取默认配置信息
        String defaultLimiterProperty = properties.getProperty(CookbookConstants.DEFAULT_LIMITER);
        // 默认限流QPS
        Double defaultProperty;
        if (StringUtils.isEmpty(defaultLimiterProperty)) {
            defaultProperty = defaultLimiter;
        }else {
            defaultProperty = Double.valueOf(defaultLimiterProperty);
        }
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String propertyName : propertyNames) {
            String property = properties.getProperty(propertyName);
            apiRateLimiterMap.put(propertyName,
                    RateLimiter.create(StringUtils.isEmpty(property) ? defaultProperty : Double.valueOf(property)));
        }
    }

    /**
     * 用于配置文件变更之后，配置信息重新生效
     * 可以通过定时任务来触发，也可以通过接口来触发
     */
    public void reloadLimiterProperty() {
        // 保存旧的配置信息，如果重新加载配置文件失败，可以用来还原
        Properties newProperties = new Properties();
        try {
            Resource resource = new PathResource(limiterPropertiesPath);
            newProperties.load(resource.getInputStream());
            // 配置文件加载成功之后，生效
            properties = newProperties;
            this.reloadLimiter();
        } catch (IOException e) {
            log.error("读取配置文件失败", e);
        }
    }

}
