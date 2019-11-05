package com.chy.summer.framework.beans.config;


import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.ConfigurableBeanFactory;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.context.event.ApplicationEventMulticaster;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;

import java.util.Set;

/**
 * ConfigurableListableBeanFactory 提供bean definition的解析,注册功能,再对单例来个预加载(解决循环依赖问题).
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory,ConfigurableBeanFactory {

    /**
     * 用了设置忽略类型
     */
    void ignoreDependencyType(Class<?> type);

    /**
     * 用了设置忽略接口
     */
    void ignoreDependencyInterface(Class<?> ifc);

    void addBeanPostProcessor(BeanPostProcessor postProcessor);

    /**
     * 用任何方式,存在于 ioc容器就行
     */
    boolean containsLocalBean(String beanName);

    /**
     * 注册一个单例对象到Ioc 容器里
     * @param beanName
     * @param singletonObject
     */
    void registerSingleton(String beanName, Object singletonObject);

    void freezeConfiguration();

    void preInstantiateSingletons();

    BeanDefinition getMergedBeanDefinition(String beanName) throws BeansException;

    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    Object resolveDependency(DependencyDescriptor desc, String beanName, Set<String> autowiredBeanNames);
}
