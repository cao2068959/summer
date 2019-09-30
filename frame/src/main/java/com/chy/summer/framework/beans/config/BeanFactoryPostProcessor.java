package com.chy.summer.framework.beans.config;


import com.chy.summer.framework.exception.BeansException;

public interface BeanFactoryPostProcessor {


    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
