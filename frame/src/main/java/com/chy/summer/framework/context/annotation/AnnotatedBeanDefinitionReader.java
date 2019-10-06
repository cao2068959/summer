package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.annotation.AnnotatedGenericBeanDefinition;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * 注释bean的定义注册器，一种替代ClassPathBeanDefinitionScanner的方法，仅适用于明确注册的类
 */
public class AnnotatedBeanDefinitionReader {

    ScopeMetadataResolver scopeMetadataResolver = null;


    /**
     * 为给定注册表创建新的AnnotatedBeanDefinitionReader。
     * @param registry
     */
    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
        scopeMetadataResolver = new AnnotationScopeMetadataResolver();
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
                             Class<? extends Annotation>[] qualifiers, BeanDefinitionCustomizer... definitionCustomizers) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);

        abd.setInstanceSupplier(instanceSupplier);
        //去解析这个class 上面的
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        if (qualifiers != null) {
            for (Class<? extends Annotation> qualifier : qualifiers) {
                if (Primary.class == qualifier) {
                    abd.setPrimary(true);
                }
                else if (Lazy.class == qualifier) {
                    abd.setLazyInit(true);
                }
                else {
                    abd.addQualifier(new AutowireCandidateQualifier(qualifier));
                }
            }
        }
        for (BeanDefinitionCustomizer customizer : definitionCustomizers) {
            customizer.customize(abd);
        }

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
    }
}
