package com.chy.summer.framework.context.imported.deferred;

import com.chy.summer.framework.context.annotation.ConfigurationClass;
import com.chy.summer.framework.context.annotation.ConfigurationClassParser;
import com.chy.summer.framework.context.annotation.ParserStrategyUtils;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeferredImportSelectorGroupingHandler {

    private final Map<Object, DeferredImportSelectorGrouping> groupings = new LinkedHashMap<>();

    private final Map<AnnotationMetadata, ConfigurationClass> configurationClasses = new HashMap<>();
    private final ConfigurationClassParser configurationClassParser;

    public DeferredImportSelectorGroupingHandler(ConfigurationClassParser configurationClassParser) {
        this.configurationClassParser = configurationClassParser;
    }

    public void register(DeferredImportSelectorHolder deferredImport) {
        Class<? extends Group> group = deferredImport.getImportSelector().getImportGroup();
        Object key = group;
        if (key == null) {
            key = deferredImport;
        }

        DeferredImportSelectorGrouping grouping = this.groupings.computeIfAbsent(key,
                mapKey -> new DeferredImportSelectorGrouping(createGroup(group)));

        grouping.add(deferredImport);
        this.configurationClasses.put(deferredImport.getConfigurationClass().getMetadata(),
                deferredImport.getConfigurationClass());
    }

    public void processGroupImports() {
        for (DeferredImportSelectorGrouping grouping : this.groupings.values()) {
            grouping.getImports().forEach(entry -> {
                ConfigurationClass configurationClass = this.configurationClasses.get(entry.getMetadata());
                try {
                    //去加载对应的 bean数据
                    configurationClassParser.processImports(configurationClass, entry.getImportClassName());
                } catch (BeanDefinitionStoreException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    throw new BeanDefinitionStoreException(
                            "Failed to process import candidates for configuration class [" +
                                    configurationClass.getMetadata().getClassName() + "]", ex);
                }
            });
        }
    }

    private Group createGroup(Class<? extends Group> type) {
        Class<? extends Group> effectiveType = (type != null ? type : DefaultDeferredImportSelectorGroup.class);
        return ParserStrategyUtils.instantiateClass(effectiveType, Group.class,
                configurationClassParser.getEnvironment(),
                configurationClassParser.getResourceLoader(),
                configurationClassParser.getRegistry());
    }
}