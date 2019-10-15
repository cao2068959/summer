package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.exception.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface InstantiationStrategy {


    Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner)
            throws BeansException;


    Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
                       Constructor<?> ctor, Object... args) throws BeansException;


    Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
                       Object factoryBean, Method factoryMethod, Object... args)
            throws BeansException;
}
