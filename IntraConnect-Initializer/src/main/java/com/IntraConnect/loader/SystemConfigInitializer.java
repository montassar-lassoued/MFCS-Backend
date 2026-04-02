package com.IntraConnect.loader;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.jdom2.Element;

public class SystemConfigInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final  Element systemConfig;
    public SystemConfigInitializer( Element systemConfig){
        this.systemConfig = systemConfig;
    }
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.getBeanFactory().registerSingleton("systemConfig", this.systemConfig);
    }
}
