package com.chy.summer.framework.boot.context.config;


import com.chy.summer.framework.context.ApplicationContextInitializer;
import com.chy.summer.framework.context.ConfigurableApplicationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelegatingApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      log.info("DelegatingApplicationContextInitializer 初始化");
    }
}
