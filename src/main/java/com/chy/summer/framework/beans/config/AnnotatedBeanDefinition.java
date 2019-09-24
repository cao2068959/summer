package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;

public interface AnnotatedBeanDefinition extends BeanDefinition {

    AnnotationMetadata getMetadata();

    MethodMetadata getFactoryMethodMetadata();
}
