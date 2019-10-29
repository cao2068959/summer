package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.core.type.MethodMetadata;
import lombok.Getter;

public abstract class ConfigurationMethod {

    @Getter
    protected final MethodMetadata metadata;


    protected final ConfigurationClass configurationClass;


    public ConfigurationMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
        this.metadata = metadata;
        this.configurationClass = configurationClass;
    }
}
