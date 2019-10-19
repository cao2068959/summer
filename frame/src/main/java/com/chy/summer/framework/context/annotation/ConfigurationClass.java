package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.util.Assert;

public class ConfigurationClass {


    private final AnnotationMetadata metadata;
    private final String beanName;

    public ConfigurationClass(AnnotationMetadata metadata, String beanName) {
        Assert.notNull(beanName, "BeanName 不能为空");
        this.metadata = metadata;
        this.beanName = beanName;
    }

    public AnnotationMetadata getMetadata() {
        return metadata;
    }
}
