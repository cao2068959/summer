package com.chy.summer.framework.boot.autoconfigure;


import com.chy.summer.framework.beans.BeanClassLoaderAware;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.context.imported.deferred.DeferredImportSelector;
import com.chy.summer.framework.context.imported.deferred.Group;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class AutoConfigurationGroup implements Group, BeanClassLoaderAware, BeanFactoryAware {


    private final List<AutoConfigurationEntry> autoConfigurationEntries = new ArrayList<>();
    private final Map<String, AnnotationMetadata> entries = new LinkedHashMap<>();


    @Override
    public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
        Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector, "[AutoConfigurationGroup] 仅能支持 [AutoConfigurationImportSelector] 类型的 deferredImportSelector");

        //获取所有需要自动配置的类的类名
        AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
                .getAutoConfigurationEntry(annotationMetadata);

        this.autoConfigurationEntries.add(autoConfigurationEntry);
        for (String importClassName : autoConfigurationEntry.getConfigurations()) {
            this.entries.putIfAbsent(importClassName, annotationMetadata);
        }
    }

    @Override
    public Iterable<Entry> selectImports() {
        if (this.autoConfigurationEntries.isEmpty()) {
            return Collections.emptyList();
        }

        //获取需要排除掉的class 如: conditional 不通过
        Set<String> allExclusions = this.autoConfigurationEntries.stream()
                .map(AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());
        Set<String> processedConfigurations = this.autoConfigurationEntries.stream()
                .map(AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        //排除
        processedConfigurations.removeAll(allExclusions);

        //TODO 这里也是提前从缓存的 spring-autoconfigure-metadata.properties 文件中去 提前获取配置信息然后排序， 这里先不做这个优化
        //processedConfigurations = sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata())

        return processedConfigurations.stream()
                .map((importClassName) -> new Entry(this.entries.get(importClassName), importClassName))
                .collect(Collectors.toList());
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {

    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {

    }


}
