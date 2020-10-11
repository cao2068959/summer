package com.chy.summer.framework.util;


import com.chy.summer.framework.annotation.stereotype.Component;
import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.annotation.Bean;
import com.chy.summer.framework.context.annotation.ComponentScan;
import com.chy.summer.framework.context.annotation.Configuration;
import com.chy.summer.framework.context.annotation.Import;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.ordered.Order;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *  配置类的 工具方法
 */
public abstract class ConfigurationClassUtils {

    //一个完整的配置类标识
    private static final String CONFIGURATION_CLASS_FULL = "full";
    //一个劣质的配置类标识
    private static final String CONFIGURATION_CLASS_LITE = "lite";

    private static final String CONFIGURATION_CLASS_ATTRIBUTE ="ConfigurationClassPostProcessor.configurationClass";
    private static final String ORDER_ATTRIBUTE ="ConfigurationClassPostProcessor.configurationClass.order";

    private static final Set<String> candidateIndicators = new HashSet<>(8);

    static {
        candidateIndicators.add(Component.class.getName());
        candidateIndicators.add(ComponentScan.class.getName());
        candidateIndicators.add(Import.class.getName());
    }

    /**
     * 检查是不是 配置类,其实也就是看有没 @Configuration 标签
     *
     * 这里如果发现对应的beanBefinition 是配置类会给他标记上 full 或者 lite
     * full: @Configuration 注解
     * lite: @Component @ComponentScan @Import 拥有任何一个都算
     *
     * @param beanDef
     * @return
     */
    public static boolean checkConfigurationClassCandidate(BeanDefinition beanDef) {
        String className = beanDef.getBeanClassName();
        if (className == null) {
            return false;
        }
        AnnotationMetadata metadata;
        if(!(beanDef instanceof AnnotatedBeanDefinition)){
            return false;
        }
        metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
        //简单的匹配有没有 @Configuration 注解
        if (isFullConfigurationCandidate(metadata)) {
            beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
        }
        //匹配有没有 @Component @ComponentScan @Import 或者 存在 @bean 方法 有任意一种都行
        else if (isLiteConfigurationCandidate(metadata)) {
            beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
        }
        else {
            return false;
        }

        //顺便看看这个 类上面有没有 @order 注解
        Integer order = getOrder(metadata);
        if (order != null) {
            beanDef.setAttribute(ORDER_ATTRIBUTE, order);
        }
        return true;
    }

    public static Integer getOrder(AnnotationMetadata metadata) {
        AnnotationAttributes orderAttributes = metadata.getAnnotationAttributes(Order.class);
        return (orderAttributes != null ? (orderAttributes.getRequiredAttribute("value",Integer.class)) : null);
    }

    public static Integer getOrder(BeanDefinition beanDefinition) {

        if(!(beanDefinition instanceof AnnotatedBeanDefinition)){
            return null;
        }

        AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
        return getOrder(metadata);
    }

    /**
     * 判断有没有注解 @Configuration
     * @param metadata
     * @return
     */
    public static boolean isFullConfigurationCandidate(AnnotationMetadata metadata) {
        return metadata.hasMetaAnnotation(Configuration.class.getName());
    }

    /**
     * 如果有注解  @Component @ComponentScan @Import 
     * 或者方法上有注解 @Bean 的也算是一个配置类
     * @param metadata
     * @return
     */
    public static boolean isLiteConfigurationCandidate(AnnotationMetadata metadata) {
        // 如果是一个接口,就不考虑了
        if (metadata.isInterface()) {
            return false;
        }

        // 这边检查是否有 @Component @ComponentScan @Import 3个注解,有任何一个 都算作配置类
        for (String indicator : candidateIndicators) {
            if (metadata.hasMetaAnnotation(indicator)) {
                return true;
            }
        }
        
        //最后看看 有没有 打了注解 @Bean 的方法,如果有的话也算你是一个配置类
        return metadata.hasAnnotatedMethods(Bean.class.getName());
    }


    /**
     * 检测 是不是配置类 有注解 @Configuration @Component @ComponentScan @Import 的就是配置类
     * @param metadata
     * @return
     */
    public static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
        return (isFullConfigurationCandidate(metadata) || isLiteConfigurationCandidate(metadata));
    }


    public static boolean isFullConfigurationClass(BeanDefinition beanDef) {
        return CONFIGURATION_CLASS_FULL.equals(beanDef.getAttribute(CONFIGURATION_CLASS_ATTRIBUTE));
    }

    public static boolean isLiteConfigurationClass(BeanDefinition beanDef) {
        return CONFIGURATION_CLASS_LITE.equals(beanDef.getAttribute(CONFIGURATION_CLASS_ATTRIBUTE));
    }
}
