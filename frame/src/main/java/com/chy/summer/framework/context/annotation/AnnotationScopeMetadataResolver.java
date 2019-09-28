package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
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
            //去尝试拿一下 这个类上打了@scope 注解的属性，如果这个类没打这个注解则拿到null
            AnnotationAttributes annotationAttributes = metadata.getAnnotationAttributes(scopeAnnotationType);
            if(annotationAttributes != null){
                ScopeType value = annotationAttributes.getRequiredAttribute("value", ScopeType.class);
                scopeMetadata.setScopeName(value);
                ScopedProxyMode proxyMode = annotationAttributes
                        .getRequiredAttribute("proxyMode", ScopedProxyMode.class);
                //如果 代理模式是默认的或者没设置，那么就用这个类上定义的默认模式
                if (proxyMode == null || proxyMode == ScopedProxyMode.DEFAULT) {
                    proxyMode = this.defaultProxyMode;
                }
                scopeMetadata.setScopedProxyMode(proxyMode);
            }
        }
        return scopeMetadata;
    }

}
