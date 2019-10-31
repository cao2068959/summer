package com.chy.summer.framework.web.servlet.context.support;

import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
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
        beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);

    }

    @Override
    protected void finishRefresh() {

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
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws IOException {

    }

    @Override
    public ApplicationContext getParent() {
        return null;
    }

    @Override
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> type) {
        return beanFactory.getBean(name,type);
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

    @Override
    public void registerAlias(String beanName, String alias) {
        beanFactory.registerAlias(beanName,alias);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return  beanFactory.getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return   beanFactory.getBeanNamesForType(type,includeNonSingletons,allowEagerInit);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return beanFactory.isTypeMatch(name,typeToMatch);
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public boolean isSingleton(String beanName) {
        return beanFactory.isSingleton(beanName);
    }

}
