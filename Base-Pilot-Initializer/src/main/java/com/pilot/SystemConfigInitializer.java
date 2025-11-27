package com.pilot;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import xml.SystemConfig;

public class SystemConfigInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final SystemConfig systemConfig;
    public SystemConfigInitializer(SystemConfig systemConfig){
        this.systemConfig = systemConfig;
    }
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.getBeanFactory().registerSingleton("systemConfig", this.systemConfig);
    }
}
