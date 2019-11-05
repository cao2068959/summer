package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;

public class ContextAnnotationAutowireCandidateResolver extends QualifierAnnotationAutowireCandidateResolver {

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return super.isAutowireCandidate(bdHolder,descriptor);
    }
}
