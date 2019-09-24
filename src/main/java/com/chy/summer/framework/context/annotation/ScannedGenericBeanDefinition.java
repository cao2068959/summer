package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.support.AbstractBeanDefinition;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;

public class ScannedGenericBeanDefinition extends AbstractBeanDefinition implements AnnotatedBeanDefinition {

    private final AnnotationMetadata metadata;

    public ScannedGenericBeanDefinition(MetadataReader metadataReader) {
        this.metadata = metadataReader.getAnnotationMetadata();
        setBeanClassName(this.metadata.getClassName());
    }

    @Override
    public AnnotationMetadata getMetadata() {
        return metadata;
    }

    @Override
    public MethodMetadata getFactoryMethodMetadata() {
        return null;
    }
}
