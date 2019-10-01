package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.exception.BeansException;

public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

    void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
}
