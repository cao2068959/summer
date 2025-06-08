package com.chy.summer.framework.beans;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import javax.annotation.Nullable;

public interface BeanFactory {

    /**
     * 工厂bean的前缀，用于将工厂bean与由其创建的bean区别开。
     * 例如，如果名为myJndiObject的bean是FactoryBean，
     * 使其变成＆myJndiObject的方式获取，将返回FactoryBean，而不是工厂返回的实例。
     */
    String FACTORY_BEAN_PREFIX = "&";

    Object getBean(String name);

    <T> T getBean(String name,Class<T> type);

    /**
     * 根据类型去获取在Ioc 容器里对应所有对象的名字
     * @param type
     * @param includeNonSingletons
     * @param allowEagerInit
     * @return
     */
    String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

    /**
     * 判断 对应 name的对象是不是对应指定的类型,有继承关系也行
     * @param name
     * @param typeToMatch
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

    public Class<?> getType(String name) throws NoSuchBeanDefinitionException;

    boolean containsBean(String beanName);


    boolean isSingleton(String beanName);
}
