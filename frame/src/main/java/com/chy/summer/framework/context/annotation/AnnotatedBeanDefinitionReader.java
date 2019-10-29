package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import com.chy.summer.framework.beans.support.BeanDefinitionReaderUtils;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * 注释bean的定义注册器，一种替代ClassPathBeanDefinitionScanner的方法，仅适用于明确注册的类
 */
public class AnnotatedBeanDefinitionReader {

    ScopeMetadataResolver scopeMetadataResolver = null;

    private BeanNameGenerator beanNameGenerator = null;

    BeanDefinitionRegistry registry;


    /**
     * 为给定注册表创建新的AnnotatedBeanDefinitionReader。
     * @param registry
     */
    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
        scopeMetadataResolver = new AnnotationScopeMetadataResolver();
        beanNameGenerator = new AnnotationBeanNameGenerator();
        //把一些公用的 beanFactroyPostProcessor 给注册进去
        AnnotationUtils.registerAnnotationConfigProcessors(registry,null);
        this.registry = registry;
    }

    public void register(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            registerBean(annotatedClass);
        }
    }

    public void registerBean(Class<?> annotatedClass) {
        doRegisterBean(annotatedClass, null, null, null);
    }

    <T> void doRegisterBean(Class<T> annotatedClass,  Supplier<T> instanceSupplier,  String name,
                             Class<? extends Annotation>[] qualifiers) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);

        abd.setInstanceSupplier(instanceSupplier);
        //去解析这个class 上面的 @scope 注解
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        //生成对应的 beanName
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, null));
        //解析一些通用注解然后设置对应值 比如@Lazy 注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd,abd.getMetadata());
        //通过显示的指定注解的形式来给 对应的 beanDefinition 来设置 值
        if (qualifiers != null) {
            for (Class<? extends Annotation> qualifier : qualifiers) {
                if (Lazy.class == qualifier) {
                    abd.setLazyInit(true);
                }
            }
        }
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        //如果@scope 注解 设置了代理模式 这里会生成代理
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        //把 BeanDefinition 放入 Ioc 容器
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
    }
}
