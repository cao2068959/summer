package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.beans.config.BeanDefinitionHolder;

public interface AutowireCandidateResolver {

    default boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return bdHolder.getBeanDefinition().isAutowireCandidate();
    }

    Object getSuggestedValue(DependencyDescriptor descriptor);
}
