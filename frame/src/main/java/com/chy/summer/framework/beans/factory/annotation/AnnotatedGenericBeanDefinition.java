package com.chy.summer.framework.beans.factory.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.support.AbstractBeanDefinition;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.core.type.StandardAnnotationMetadata;


/**
 * 这个 BeanDefinition 将会解析 class里面所有的注解,有一个 AnnotationMetadata 对象来保存这些注解
 *
 */
public class AnnotatedGenericBeanDefinition extends AbstractBeanDefinition implements AnnotatedBeanDefinition {


    private final AnnotationMetadata metadata;

    private MethodMetadata factoryMethodMetadata;


    public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
        //解析 class 上面所有的注解,生成一个 AnnotationMetadata 对象来存放这些所有的注解
        this.metadata = new StandardAnnotationMetadata(beanClass, true);
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
        if (metadata instanceof StandardAnnotationMetadata) {
            setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
        }
        else {
            setBeanClassName(metadata.getClassName());
        }
        this.metadata = metadata;
    }

    @Override
    public AnnotationMetadata getMetadata() {
        return metadata;
    }

    @Override
    public MethodMetadata getFactoryMethodMetadata() {
        return factoryMethodMetadata;
    }

}
