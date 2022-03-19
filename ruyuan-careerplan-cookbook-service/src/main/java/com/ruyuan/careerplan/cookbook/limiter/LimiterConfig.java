package com.ruyuan.careerplan.cookbook.limiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhonghuashishan
 */
@Configuration
public class LimiterConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(getLimiterInterceptor());
        registration.addPathPatterns("/api/**");
    }

    @Bean
    public HandlerInterceptor getLimiterInterceptor() {
        return new LimiterInterceptor();
    }
}
