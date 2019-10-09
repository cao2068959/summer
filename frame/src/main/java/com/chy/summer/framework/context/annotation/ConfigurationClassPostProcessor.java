package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistryPostProcessor;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.core.PriorityOrdered;
import com.chy.summer.framework.exception.BeansException;

public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
        PriorityOrdered {


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("执行了 postProcessBeanDefinitionRegistry");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("执行了 postProcessBeanFactory");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
