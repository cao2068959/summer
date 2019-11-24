package com.chy.summer.framework.beans.support;


import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;

public interface BeanNameGenerator {

    String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);




}
