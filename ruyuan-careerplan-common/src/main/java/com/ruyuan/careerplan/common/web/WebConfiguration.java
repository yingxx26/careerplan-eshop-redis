package com.ruyuan.careerplan.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruyuan.careerplan.common.core.DateProvider;
import com.ruyuan.careerplan.common.core.DateProviderImpl;
import com.ruyuan.careerplan.common.core.ObjectMapperImpl;
import com.ruyuan.careerplan.common.json.JsonExtractor;
import com.ruyuan.careerplan.common.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * web相关bean组件配置
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Configuration
@Import(value = {GlobalExceptionHandler.class, GlobalResponseBodyAdvice.class})
public class WebConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapperImpl();
    }

    @Bean
    public DateProvider dateProvider() {
        return new DateProviderImpl();
    }

    @Bean
    public JsonExtractor jsonExtractor() {
        return new JsonExtractor();
    }
}