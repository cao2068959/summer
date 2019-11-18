package com.chy.summer.framework.aop.scope;

import com.chy.summer.framework.aop.framework.autoProxy.AutoProxyUtils;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.sun.istack.internal.Nullable;

/**
 * 用于创建作用域代理的实用程序类。 由ScopedProxyBeanDefinitionDecorator和ClassPathBeanDefinitionScanner使用
 */
public abstract class ScopedProxyUtils {

    private static final String TARGET_NAME_PREFIX = "scopedTarget.";


    /**
     * 为提供的目标bean生成作用域代理，用内部名称注册目标bean并在作用域代理上设置'targetBeanName'。
     *
     * @param definition       原始bean定义
     * @param registry         bean定义注册表
     * @param proxyTargetClass 是否创建目标类代理
     */
    public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,
                                                         BeanDefinitionRegistry registry, boolean proxyTargetClass) {

        String originalBeanName = definition.getBeanName();
        BeanDefinition targetDefinition = definition.getBeanDefinition();
        String targetBeanName = getTargetBeanName(originalBeanName);

        //为原始bean名创建作用域代理定义，在内部目标定义中“隐藏”目标bean。
        RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
        proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
        //TODO GYX 缺少属性
//		proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
//		proxyDefinition.setSource(definition.getSource());
//		proxyDefinition.setRole(targetDefinition.getRole());

        proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
        if (proxyTargetClass) {
            targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        } else {
            proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
        }

        //从原始bean定义复制自动装配设置
        proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
        proxyDefinition.setPrimary(targetDefinition.isPrimary());
//		if (targetDefinition instanceof AbstractBeanDefinition) {
//			proxyDefinition.copyQualifiersFrom((AbstractBeanDefinition) targetDefinition);
//		}

        //应该忽略目标bean，而使用范围确定的代理。
        targetDefinition.setAutowireCandidate(false);
        targetDefinition.setPrimary(false);

        // 在工厂中将目标bean注册为单独的bean。
        registry.registerBeanDefinition(targetBeanName, targetDefinition);

        //返回作用域代理定义作为主bean定义
        return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
    }

    /**
     * 生成作用域代理内用于引用目标bean的bean名称
     *
     * @param originalBeanName bean的原始名称
     */
    public static String getTargetBeanName(String originalBeanName) {
        return TARGET_NAME_PREFIX + originalBeanName;
    }

    /**
     * 判断beanName是否是在作用域代理中引用目标bean的bean名称
     */
    public static boolean isScopedTarget(@Nullable String beanName) {
        return (beanName != null && beanName.startsWith(TARGET_NAME_PREFIX));
    }

}