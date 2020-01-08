package com.chy.summer.framework.context.annotation.condition;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;

/**
 *  让 confition 使用的上下文对象,可以在里面拿到各种容器的重要信息
 */
public interface ConditionContext {

    /**
     * 获取 bd 注册器
     * @return
     */
    BeanDefinitionRegistry getRegistry();


    /**
     * 获取 beanfactory
     * @return
     */
    ConfigurableListableBeanFactory getBeanFactory();


    /**
     * 获取 配置文件上下文
     * @return
     */
    Environment getEnvironment();


    /**
     * 获取 资源文件加载器
     * @return
     */
    ResourceLoader getResourceLoader();


    /**
     * 获取 类加载器
     * @return
     */
    ClassLoader getClassLoader();

}
