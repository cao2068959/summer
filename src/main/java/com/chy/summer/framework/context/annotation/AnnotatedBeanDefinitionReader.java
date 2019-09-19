package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;

/**
 * 注释bean的定义注册器，一种替代ClassPathBeanDefinitionScanner的方法，仅适用于明确注册的类
 */
public class AnnotatedBeanDefinitionReader {
    /**
     * 为给定注册表创建新的AnnotatedBeanDefinitionReader。
     * @param registry
     */
    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {

    }
}
