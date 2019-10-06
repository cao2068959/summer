package com.chy.summer.framework.context;

import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;

public interface ConfigurableApplicationContext extends ApplicationContext {

    void setEnvironment(ConfigurableEnvironment environment);

    ConfigurableListableBeanFactory getBeanFactory();
}
