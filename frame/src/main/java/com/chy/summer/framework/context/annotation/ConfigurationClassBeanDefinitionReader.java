package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.util.StringUtils;

import java.util.Map;
import java.util.Set;

public class ConfigurationClassBeanDefinitionReader {

    private static final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private final BeanDefinitionRegistry registry;

    private final ResourceLoader resourceLoader;

    private final Environment environment;

    private final BeanNameGenerator importBeanNameGenerator;


    ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry,
                                           ResourceLoader resourceLoader, Environment environment, BeanNameGenerator importBeanNameGenerator) {

        this.registry = registry;
        this.resourceLoader = resourceLoader;
        this.environment = environment;
        this.importBeanNameGenerator = importBeanNameGenerator;
    }

    public void loadBeanDefinitions(Set<ConfigurationClass> configClasses) {
        for (ConfigurationClass configClass : configClasses) {
            loadBeanDefinitionsForConfigurationClass(configClass);
        }
    }

    private void loadBeanDefinitionsForConfigurationClass(
            ConfigurationClass configClass) {

        //这里先把 这个类上面的 所有 @Import 标注出来的类先解析了,然后注册进入 IOC
        if (configClass.isImported()) {
            registerBeanDefinitionForImportedConfigurationClass(configClass);
        }
        //这里开始解析 @Bean 的方法
        for (BeanMethod beanMethod : configClass.getBeanMethods()) {
            loadBeanDefinitionsForBeanMethod(beanMethod);
        }
        loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
    }

    private void loadBeanDefinitionsForBeanMethod(BeanMethod beanMethod) {

    }

    private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {
        registrars.forEach((registrar, metadata) ->
                registrar.registerBeanDefinitions(metadata, this.registry));
    }


    /**
     * 把 @Import 上标注过的类 解析 并且 注册进入 ioc 容器,成为 beanDefinition
     * @param configClass
     */
    private void registerBeanDefinitionForImportedConfigurationClass(ConfigurationClass configClass) {
        AnnotationMetadata metadata = configClass.getMetadata();
        AnnotatedGenericBeanDefinition configBeanDef = new AnnotatedGenericBeanDefinition(metadata);
        //解析作用域 @Scope 注解的解析
        ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(configBeanDef);
        configBeanDef.setScope(scopeMetadata.getScopeName());
        //生成beanName
        String configBeanName = this.importBeanNameGenerator.generateBeanName(configBeanDef, this.registry);
        AnnotationConfigUtils.processCommonDefinitionAnnotations(configBeanDef, metadata);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(configBeanDef, configBeanName);
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        this.registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
        configClass.setBeanName(configBeanName);

    }
}
