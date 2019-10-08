package com.chy.summer.framework.boot.autoconfigure;


import com.chy.summer.framework.context.ApplicationContextInitializer;
import com.chy.summer.framework.context.ConfigurableApplicationContext;
import com.chy.summer.framework.core.ordered.Ordered;

public class SharedMetadataReaderFactoryContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
