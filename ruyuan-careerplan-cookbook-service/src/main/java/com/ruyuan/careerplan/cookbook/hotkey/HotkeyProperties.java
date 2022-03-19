package com.ruyuan.careerplan.cookbook.hotkey;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * hotkey的配置信息
 *
 * @author zhonghuashishan
 */
@Data
@Component
@ConfigurationProperties(prefix = "hotkey")
public class HotkeyProperties {

    private String appName;

    private String etcdServer;

    private Integer caffeineSize;

    private Long pushPeriod;

}