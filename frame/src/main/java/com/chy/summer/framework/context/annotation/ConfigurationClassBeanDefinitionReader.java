package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.chy.summer.framework.context.annotation.constant.Autowire;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

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

    /**
     * 根据配置类里面的信息,把一些需要的bean 注册进入 ioc里面
     * 说白点就是把 配置类里面 @Bean 标注的方法生成的类注册进 ioc 里面,当然 @Bean 只是其中一种方式
     * @param configClass
     */
    private void loadBeanDefinitionsForConfigurationClass(
            ConfigurationClass configClass) {

        //这里先把 这个类上面的 所有 @Import 标注出来的类先解析了,然后注册进入 IOC
        if (configClass.isImported()) {
            registerBeanDefinitionForImportedConfigurationClass(configClass);
        }
        //将 被 @Bean 标记的方法生成 对应的bean 注册进ioc中
        for (BeanMethod beanMethod : configClass.getBeanMethods()) {
            loadBeanDefinitionsForBeanMethod(beanMethod);
        }
        loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
    }

    /**
     * 解析 @Bean 标记的方法, 执行这个方法,把返回值给放入 Ioc 容器
     * @param beanMethod
     */
    private void loadBeanDefinitionsForBeanMethod(BeanMethod beanMethod) {
        //把这个方法持有的类给先拿出来
        ConfigurationClass configClass = beanMethod.getConfigurationClass();
        MethodMetadata metadata = beanMethod.getMetadata();
        String methodName = metadata.getMethodName();

        // TODO 这是在配合注解 @Condition 使用的时候来自动判断是否去加载对应的bean
       /* if (this.conditionEvaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
            configClass.skippedBeanMethods.add(methodName);
            return;
        }
        if (configClass.skippedBeanMethods.contains(methodName)) {
            return;
        }*/

        //去拿 @Bean 注解上的所有属性
        AnnotationAttributes beanAnnotationAttributes = metadata.getAnnotationAttributes(Bean.class.getName());
        Assert.state(beanAnnotationAttributes != null, "No @Bean annotation attributes");

        //去拿@Bean 注解上面设置的 name的属性值
        List<String> names = Arrays
                .asList(beanAnnotationAttributes.getRequiredAttribute("name", String[].class));
        //如果设置了 多个beanName 则拿第一个当做 beanName , 其他的全是别名
        //如果没手动设置beanName 就用方法名当做beanName
        String beanName = (!names.isEmpty() ? names.remove(0) : methodName);

        //把别名注册进去
        for (String alias : names) {
            this.registry.registerAlias(beanName, alias);
        }

        //包装了一下 父类其实就是 RootBeanDefinition
        ConfigurationClassBeanDefinition beanDef = new ConfigurationClassBeanDefinition(configClass, metadata);
        beanDef.setResource(configClass.getResource());

        if (metadata.isStatic()) {
            // static @Bean method
            beanDef.setBeanClassName(configClass.getMetadata().getClassName());
            beanDef.setFactoryMethodName(methodName);
        }
        else {
            // instance @Bean method
            beanDef.setFactoryBeanName(configClass.getBeanName());
            beanDef.setUniqueFactoryMethodName(methodName);
        }

        //解析@Lazy @Primary @DependsOn @Role  @Description 注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDef, metadata);

        Autowire autowire = beanAnnotationAttributes.getRequiredAttribute("autowire",Autowire.class);
        if (autowire.isAutowire()) {
            beanDef.setAutowireMode(autowire.value());
        }

        String initMethodName = bean.getString("initMethod");
        if (StringUtils.hasText(initMethodName)) {
            beanDef.setInitMethodName(initMethodName);
        }

        String destroyMethodName = bean.getString("destroyMethod");
        beanDef.setDestroyMethodName(destroyMethodName);

        // Consider scoping
        ScopedProxyMode proxyMode = ScopedProxyMode.NO;
        AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(metadata, Scope.class);
        if (attributes != null) {
            beanDef.setScope(attributes.getString("value"));
            proxyMode = attributes.getEnum("proxyMode");
            if (proxyMode == ScopedProxyMode.DEFAULT) {
                proxyMode = ScopedProxyMode.NO;
            }
        }

        // Replace the original bean definition with the target one, if necessary
        BeanDefinition beanDefToRegister = beanDef;
        if (proxyMode != ScopedProxyMode.NO) {
            BeanDefinitionHolder proxyDef = ScopedProxyCreator.createScopedProxy(
                    new BeanDefinitionHolder(beanDef, beanName), this.registry,
                    proxyMode == ScopedProxyMode.TARGET_CLASS);
            beanDefToRegister = new ConfigurationClassBeanDefinition(
                    (RootBeanDefinition) proxyDef.getBeanDefinition(), configClass, metadata);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Registering bean definition for @Bean method %s.%s()",
                    configClass.getMetadata().getClassName(), beanName));
        }

        this.registry.registerBeanDefinition(beanName, beanDefToRegister);
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
        //注册进入ioc容器
        this.registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
        configClass.setBeanName(configBeanName);
    }

    private static class ConfigurationClassBeanDefinition extends RootBeanDefinition implements AnnotatedBeanDefinition {

        private final AnnotationMetadata annotationMetadata;

        private final MethodMetadata factoryMethodMetadata;

        public ConfigurationClassBeanDefinition(ConfigurationClass configClass, MethodMetadata beanMethodMetadata) {
            this.annotationMetadata = configClass.getMetadata();
            this.factoryMethodMetadata = beanMethodMetadata;
        }

        public ConfigurationClassBeanDefinition(
                RootBeanDefinition original, ConfigurationClass configClass, MethodMetadata beanMethodMetadata) {
            super(original);
            this.annotationMetadata = configClass.getMetadata();
            this.factoryMethodMetadata = beanMethodMetadata;
        }

        private ConfigurationClassBeanDefinition(ConfigurationClassBeanDefinition original) {
            super(original);
            this.annotationMetadata = original.annotationMetadata;
            this.factoryMethodMetadata = original.factoryMethodMetadata;
        }

        @Override
        public AnnotationMetadata getMetadata() {
            return this.annotationMetadata;
        }

        @Override
        public MethodMetadata getFactoryMethodMetadata() {
            return this.factoryMethodMetadata;
        }


        @Override
        public ConfigurationClassBeanDefinition cloneBeanDefinition() {
            return new ConfigurationClassBeanDefinition(this);
        }
    }


}
