package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.support.AbstractBeanDefinition;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import lombok.Getter;

public class ScannedGenericBeanDefinition extends AbstractBeanDefinition implements AnnotatedBeanDefinition {

    @Getter
    private  AnnotationMetadata metadata;

    private String parentName;




    public ScannedGenericBeanDefinition(MetadataReader metadataReader) {
        this.metadata = metadataReader.getAnnotationMetadata();
        setBeanClassName(this.metadata.getClassName());

    }


    @Override
    public MethodMetadata getFactoryMethodMetadata() {
        return null;
    }

    @Override
    public void setFactoryBeanName(String factoryBeanName) {

    }

    @Override
    public String getFactoryBeanName() {
        return null;
    }

    @Override
    public String getFactoryMethodName() {
        return null;
    }
}
