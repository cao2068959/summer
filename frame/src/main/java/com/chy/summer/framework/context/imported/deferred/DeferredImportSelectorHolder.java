package com.chy.summer.framework.context.imported.deferred;


import com.chy.summer.framework.context.annotation.ConfigurationClass;

public class DeferredImportSelectorHolder {

    private final ConfigurationClass configurationClass;

    private final DeferredImportSelector importSelector;

    public DeferredImportSelectorHolder(ConfigurationClass configurationClass, DeferredImportSelector importSelector) {
        this.configurationClass = configurationClass;
        this.importSelector = importSelector;
    }

    public ConfigurationClass getConfigurationClass() {
        return configurationClass;
    }

    public DeferredImportSelector getImportSelector() {
        return importSelector;
    }
}
