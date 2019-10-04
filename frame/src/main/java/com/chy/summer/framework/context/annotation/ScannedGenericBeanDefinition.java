package com.chy.summer.framework.context.annotation;


import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.support.AbstractBeanDefinition;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;

public class ScannedGenericBeanDefinition extends AbstractBeanDefinition implements AnnotatedBeanDefinition {

    private final AnnotationMetadata metadata;

    private String parentName;




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

    @Override
    public void setParentName(String parentName) {

    }

    @Override
    public String getParentName() {
        return parentName;
    }

    @Override
    public void setDependsOn(String... dependsOn) {

    }

    @Override
    public String[] getDependsOn() {
        return new String[0];
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {

    }

    @Override
    public boolean isAutowireCandidate() {
        return false;
    }

    @Override
    public void setPrimary(boolean primary) {

    }

    @Override
    public boolean isPrimary() {
        return false;
    }

    @Override
    public void setFactoryBeanName(String factoryBeanName) {

    }

    @Override
    public String getFactoryBeanName() {
        return null;
    }

    @Override
    public void setFactoryMethodName(String factoryMethodName) {

    }

    @Override
    public String getFactoryMethodName() {
        return null;
    }

    @Override
    public boolean isPrototype() {
        return false;
    }

    @Override
    public String getResourceDescription() {
        return null;
    }
}
