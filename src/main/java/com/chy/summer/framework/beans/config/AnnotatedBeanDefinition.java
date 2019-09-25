package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;

import java.lang.annotation.Annotation;

public interface AnnotatedBeanDefinition extends BeanDefinition {

    AnnotationMetadata getMetadata();

    MethodMetadata getFactoryMethodMetadata();


}
