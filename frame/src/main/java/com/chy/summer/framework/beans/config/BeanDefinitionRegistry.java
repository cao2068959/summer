package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;

/**
 * 用于保存bean定义的注册表的接口
 *
 * bean工厂包中封装bean定义注册的唯一接口。标准的BeanFactory接口只会覆盖对完全配置的工厂实例的访问
 * bean定义注册器可以处理这个接口的实现类
 */
public interface BeanDefinitionRegistry {

    /**
     * 查看在 容器内 是否存在对应name 的BeanDefinition
     * @param beanName
     * @return
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 根据名称拿 BeanDefinition
     * @param beanName
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 把 beanDefinition 注册进 容器中
     * @param beanName
     * @param beanDefinition
     * @throws BeanDefinitionStoreException
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
            throws BeanDefinitionStoreException;

    /**
     * 把别名和真实的beanName 给关联起来
     * @param beanName
     * @param alias
     */
    void registerAlias(String beanName, String alias);

    String[] getBeanDefinitionNames();

    /**
     * 从ioc 容器里把指定 beanName 的beandefinition 给删除
     * @param beanName
     */
    void removeBeanDefinition(String beanName);
}
