package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinition;

public interface ScopeMetadataResolver {

    ScopeMetadata resolveScopeMetadata(BeanDefinition definition);
}
