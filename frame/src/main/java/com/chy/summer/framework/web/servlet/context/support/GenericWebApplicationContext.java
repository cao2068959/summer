package com.chy.summer.framework.web.servlet.context.support;

import com.chy.summer.framework.Exception.BeanDefinitionStoreException;
import com.chy.summer.framework.Exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.support.AbstractApplicationContext;

import java.io.IOException;

public class GenericWebApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {


    private final DefaultListableBeanFactory beanFactory;

    public GenericWebApplicationContext() {
        this.beanFactory = new DefaultListableBeanFactory();
    }

    @Override
    public final ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public void freshBeanFactory() {
        //TODO 如果要对 beanFactory 设置一些初始化的玩意走这里
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

    }

    @Override
    public ApplicationContext getParent() {
        return null;
    }

    @Override
    public Object getBean(String name) {
        return null;
    }

    //===============BeanDefinitionRegistry 的实现实际上都是调用DefaultListableBeanFactory真正的实现===================
    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        beanFactory.registerBeanDefinition(beanName,beanDefinition);
    }
}
