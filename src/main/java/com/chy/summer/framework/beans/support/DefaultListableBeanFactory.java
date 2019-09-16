package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.support.AbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory  {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
}
