package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanPostProcessor;

public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

    void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);


    /**
     * beanDefinition 被移除的时候会触发
     * @param beanName
     */
    default void resetBeanDefinition(String beanName) {
    }
}
