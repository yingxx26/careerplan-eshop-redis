package com.ruyuan.careerplan.cookbook.hotkey;

import com.jd.platform.hotkey.client.ClientStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author zhonghuashishan
 */
@Slf4j
@Component
@EnableConfigurationProperties(HotkeyProperties.class)
public class HotkeyConfig {

    /**
     * 配置内容对象
     */
    @Autowired
    private HotkeyProperties hotkeyProperties;


    @PostConstruct
    public void initHotkey() {
        log.info("init hotkey, appName:{}, etcdServer:{}, caffeineSize:{}, pushPeriod:{}",
                hotkeyProperties.getAppName(), hotkeyProperties.getEtcdServer(),
                hotkeyProperties.getCaffeineSize(), hotkeyProperties.getPushPeriod());
        ClientStarter.Builder builder = new ClientStarter.Builder();
        ClientStarter starter = builder.setAppName(hotkeyProperties.getAppName())
                .setEtcdServer(hotkeyProperties.getEtcdServer())
                .setCaffeineSize(hotkeyProperties.getCaffeineSize())
                .setPushPeriod(hotkeyProperties.getPushPeriod())
                .build();
        starter.startPipeline();
    }
}
