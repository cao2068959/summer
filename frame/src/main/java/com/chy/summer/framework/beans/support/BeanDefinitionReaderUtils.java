package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;

/**
 * BeanDefinition 的工具类
 */
public class BeanDefinitionReaderUtils {

    /**
     * 把BeanDefinition 注册进 容器
     */
    public static void registerBeanDefinition(BeanDefinitionHolder definitionHolder,
                                              BeanDefinitionRegistry registry) {
        String beanName = definitionHolder.getBeanName();
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
        //如果设置了别名,也要设置进去
        String[] aliases = definitionHolder.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                registry.registerAlias(beanName, alias);
            }
        }
    }
}
