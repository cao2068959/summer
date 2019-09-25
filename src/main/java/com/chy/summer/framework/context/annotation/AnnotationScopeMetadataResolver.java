package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.annotation.constant.ScopedProxyMode;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

public class AnnotationScopeMetadataResolver implements ScopeMetadataResolver {


    private final ScopedProxyMode defaultProxyMode;

    protected Class<? extends Annotation> scopeAnnotationType = Scope.class;

    public AnnotationScopeMetadataResolver() {
        defaultProxyMode = ScopedProxyMode.NO;
    }


    @Override
    public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
        ScopeMetadata scopeMetadata = new ScopeMetadata();
        if (definition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
            AnnotationMetadata metadata = annDef.getMetadata();
            AnnotationAttributes annotationAttributes = metadata.getAnnotationAttributes(scopeAnnotationType);
            if(annotationAttributes != null){

            }

        }
        return scopeMetadata;
    }

}
