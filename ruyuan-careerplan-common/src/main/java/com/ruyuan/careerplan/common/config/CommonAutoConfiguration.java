package com.ruyuan.careerplan.common.config;

import com.ruyuan.careerplan.common.aspect.RedisAspect;
import com.ruyuan.careerplan.common.bean.SpringApplicationContext;
import com.ruyuan.careerplan.common.redis.RedisConfig;
import com.ruyuan.careerplan.common.web.WebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Configuration
@Import(value = {WebConfiguration.class, RedisConfig.class, SpringApplicationContext.class, RedisAspect.class})
public class CommonAutoConfiguration {

}