package com.chy.summer.framework.beans.support;


import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.util.ClassUtils;

import java.beans.Introspector;

/**
 * 默认的 beanName 生成器,直接用类型判断
 */
public class DefaultBeanNameGenerator implements BeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        return buildDefaultBeanName(definition);
    }

    protected String buildDefaultBeanName(BeanDefinition definition) {
        String shortClassName = ClassUtils.getShortName(definition.getBeanClassName());
        return Introspector.decapitalize(shortClassName);
    }

}
