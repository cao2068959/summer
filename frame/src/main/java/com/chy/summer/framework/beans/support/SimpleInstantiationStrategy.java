package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanUtils;
import com.chy.summer.framework.exception.BeanInstantiationException;
import com.chy.summer.framework.exception.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class SimpleInstantiationStrategy implements InstantiationStrategy {

    @Override
    public Object instantiate(RootBeanDefinition bd,  String beanName, BeanFactory owner) {
        if (bd.hasMethodOverrides()) {
            //TODO 这里会用cglib去创建对象
        }
        //获取BeanDefinition 里的构造器
        Constructor<?> constructorToUse = (Constructor<?>) bd.resolvedConstructorOrFactoryMethod;
        //如果没有在 beanDefintion 里设置过无参构造器,那么就直接从class里获取
        if (constructorToUse == null) {
            final Class<?> clazz = bd.getBeanClass();
            if (clazz.isInterface()) {
                throw new BeanInstantiationException("[%s] 是一个接口不能够实例化",clazz);
            }
            try {
                constructorToUse =	clazz.getDeclaredConstructor();
                bd.resolvedConstructorOrFactoryMethod = constructorToUse;
            }
            catch (Throwable ex) {
                throw new BeanInstantiationException("类 [%s] 的无参构造器没有发现",clazz);
            }
        }
        //实例化对象
        return BeanUtils.instantiateClass(constructorToUse);
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) throws BeansException {
        return null;
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Object factoryBean, Method factoryMethod, Object... args) throws BeansException {
        return null;
    }

}

