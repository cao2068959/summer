package com.chy.summer.framework.beans.config;


import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.context.event.ApplicationEventMulticaster;

public interface ConfigurableListableBeanFactory extends BeanFactory {

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
}
