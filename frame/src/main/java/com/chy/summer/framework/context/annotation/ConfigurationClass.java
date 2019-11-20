package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.StandardAnnotationMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.util.Assert;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationClass {

    private final AnnotationMetadata metadata;
    @Getter
    private  Resource resource;
    @Getter
    @Setter
    private  String beanName;

    @Getter
    private final Set<BeanMethod> beanMethods = new LinkedHashSet<>();

    /**
     * 被委托对象和委托注解的关系
     */
    private final Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> importBeanDefinitionRegistrars =
            new LinkedHashMap<>();


    //这个 类被多个 @Import 指明要注入
    private final Set<ConfigurationClass> importedBy = new LinkedHashSet<>(1);

    public ConfigurationClass(AnnotationMetadata metadata, String beanName) {
        Assert.notNull(beanName, "BeanName 不能为空");
        this.metadata = metadata;
        this.beanName = beanName;
    }

    public ConfigurationClass(Class<?> clazz,  ConfigurationClass importedBy) {
        this.metadata = new StandardAnnotationMetadata(clazz, true);
        this.importedBy.add(importedBy);
    }

    public ConfigurationClass(MetadataReader metadataReader,  ConfigurationClass importedBy) {
        this.metadata = metadataReader.getAnnotationMetadata();
        this.resource = metadataReader.getResource();
        this.importedBy.add(importedBy);
    }

    public AnnotationMetadata getMetadata() {
        return metadata;
    }

    public void addBeanMethod(BeanMethod method) {
        this.beanMethods.add(method);
    }

    public boolean isImported() {
        return false;
    }

    public void addImportBeanDefinitionRegistrar(ImportBeanDefinitionRegistrar registrar, AnnotationMetadata importingClassMetadata) {
        this.importBeanDefinitionRegistrars.put(registrar, importingClassMetadata);
    }

    public Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> getImportBeanDefinitionRegistrars() {
        return this.importBeanDefinitionRegistrars;
    }
}
