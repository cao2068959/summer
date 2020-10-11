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
 *  BeanDefinitionReader 之一
 *
 *  register(Class<?>... annotatedClasses)  为这个加载器的入口, 将会去解析传入class的注解,并且把他这个class 注册进入ioc容器中
 *
 *
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

    /**
     * 把入参进来的类给生成对应的 BeanDefinition 然后注册进入 ioc容器里面
     * @param annotatedClasses
     */
    public void register(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            doRegisterBean(annotatedClass, null, null, null);
        }
    }


    /**
     *
     * 把扫描某个类的注解,如果符合条件就把他注册进入ioc容器中
     *
     * @param annotatedClass  要注册的class
     * @param instanceSupplier 实例的父类
     * @param name             是否要指定对应的beanName 没有就直接用类名生成了
     * @param qualifiers      通过显示的指定注解的形式来给 对应的 beanDefinition 来设置 值 ,比如 可以传入 一个 @Lazy 来设置这个类是否懒加载
     */
    public <T> void doRegisterBean(Class<T> annotatedClass,  Supplier<T> instanceSupplier,  String name,
                             Class<? extends Annotation>[] qualifiers) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);

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
