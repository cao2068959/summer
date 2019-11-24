package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.annotation.stereotype.Component;
import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.beans.support.DefaultBeanNameGenerator;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;

import java.beans.Introspector;
import java.lang.annotation.Annotation;

/**
 * 根据@Component注解 生成 beanName
 */
public class AnnotationBeanNameGenerator extends DefaultBeanNameGenerator {

    private final Class<? extends Annotation> generatorAnnotationType = Component.class;


    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        String beanName = determineBeanNameFromAnnotation(definition);
        if (!(StringUtils.isEmpty(beanName))) {
            return beanName;
        }
        //如果没有指定注解，就使用默认的名字生成方式
        return super.generateBeanName(definition, registry);
    }

    private String determineBeanNameFromAnnotation(BeanDefinition definition) {
        if (!(definition instanceof AnnotatedBeanDefinition)) {
            return null;
        }
        AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) definition;
        AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
        //获取 Component 注解上看有没有手动指定了 beanName
        AnnotationAttributes annotationAttributes = metadata.getAnnotationAttributes(generatorAnnotationType);
        String value = annotationAttributes.getRequiredAttribute("value", String.class);
        //如果在@Component 以及对应的派生注解里设置了 value值，则这个值就作为beanName
        if (!StringUtils.isEmpty(value)) {
            return value;
        }
        return null;
    }

}
