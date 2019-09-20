package com.chy.summer.framework.web.servlet.context.support;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.support.AbstractApplicationContext;

import java.io.IOException;

public class GenericWebApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {


    @Override
    public ApplicationContext getParent() {
        return null;
    }


    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        return null;
    }

    @Override
    public void freshBeanFactory() {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws IOException {

    }

    @Override
    public Object getBean(String name) {
        return null;
    }
}
