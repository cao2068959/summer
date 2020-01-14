package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.chy.summer.framework.context.annotation.condition.ConditionEvaluator;
import com.chy.summer.framework.context.annotation.condition.ConfigurationCondition;
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
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class ConfigurationClassBeanDefinitionReader {

    private static final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private final BeanDefinitionRegistry registry;

    private final ResourceLoader resourceLoader;

    private final Environment environment;

    private final BeanNameGenerator importBeanNameGenerator;

    private TrackedConditionEvaluator trackedConditionEvaluator = new TrackedConditionEvaluator();

    private final ConditionEvaluator conditionEvaluator;

    ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry,
                                           ResourceLoader resourceLoader, Environment environment, BeanNameGenerator importBeanNameGenerator) {

        this.registry = registry;
        this.resourceLoader = resourceLoader;
        this.environment = environment;
        this.importBeanNameGenerator = importBeanNameGenerator;
        this.conditionEvaluator = new ConditionEvaluator(registry, environment, resourceLoader);
    }

    public void loadBeanDefinitions(Set<ConfigurationClass> configClasses) {
        for (ConfigurationClass configClass : configClasses) {
            loadBeanDefinitionsForConfigurationClass(configClass);
        }
    }

    /**
     * 根据配置类里面的信息,把一些需要的bean 注册进入 ioc里面
     * 说白点就是把 配置类里面 @Bean 标注的方法生成的类注册进 ioc 里面,当然 @Bean 只是其中一种方式
     *
     * @param configClass
     */
    private void loadBeanDefinitionsForConfigurationClass(
            ConfigurationClass configClass) {
        //判断这个 bean 是否应该跳过配置,这里其实就是去检查 @conditional 注解,判断里面的逻辑
        if (trackedConditionEvaluator.shouldSkip(configClass)) {
            String beanName = configClass.getBeanName();
            //如果真的要跳过,那么这个beandefinition 可能已经注册进入Ioc里面了,这里需要把他删除
            if (StringUtils.hasLength(beanName) && this.registry.containsBeanDefinition(beanName)) {
                this.registry.removeBeanDefinition(beanName);
            }
            return;
        }

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
     *
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
        } else {
            // instance @Bean method
            beanDef.setFactoryBeanName(configClass.getBeanName());
            beanDef.setUniqueFactoryMethodName(methodName);
        }

        //解析@Lazy @Primary @DependsOn @Role  @Description 注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDef, metadata);

        Autowire autowire = beanAnnotationAttributes.getRequiredAttribute("autowire", Autowire.class);
        if (autowire.isAutowire()) {
            beanDef.setAutowireMode(autowire);
        }

        String initMethodName = beanAnnotationAttributes.getRequiredAttribute("initMethod", String.class);
        if (StringUtils.hasText(initMethodName)) {
            beanDef.setInitMethodName(initMethodName);
        }

        String destroyMethodName = beanAnnotationAttributes.getRequiredAttribute("destroyMethod", String.class);
        beanDef.setDestroyMethodName(destroyMethodName);

        log.debug("准备注册 来自 @Bean 的 beanDefinition ---> [{}].[{}] ", configClass.getMetadata().getClassName(), beanName);
        this.registry.registerBeanDefinition(beanName, beanDef);
    }


    private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {
        registrars.forEach((registrar, metadata) ->
                registrar.registerBeanDefinitions(metadata, this.registry));
    }


    /**
     * 把 @Import 上标注过的类 解析 并且 注册进入 ioc 容器,成为 beanDefinition
     *
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

        public boolean isFactoryMethod(Method candidate) {
            return (super.isFactoryMethod(candidate) && BeanAnnotationHelper.isBeanAnnotated(candidate));
        }

    }

    /**
     * 用了记录 shouldSkip 执行之后的情况,避免 同一个ConfigurationClass重复判断
     */
    private class TrackedConditionEvaluator {

        private final Map<ConfigurationClass, Boolean> skipped = new HashMap<>();

        public boolean shouldSkip(ConfigurationClass configClass) {
            //先看缓存里有没有,有的话就直接给缓存里的结果
            Boolean skip = this.skipped.get(configClass);
            if (skip != null) {
                return skip;
            }
            if (configClass.isImported()) {
                boolean allSkipped = true;
                //如果这个 configClass 是从@Import 导入进去的,那么找到他所有的持有对象(就是标记了@Import的那个类)
                //先检查持有类上的 conditional 条件,只要有一个通过,那么就通过
                for (ConfigurationClass importedBy : configClass.getImportedBy()) {
                    if (!shouldSkip(importedBy)) {
                        allSkipped = false;
                        break;
                    }
                }
                if (allSkipped) {
                    skip = true;
                }
            }

            if (skip == null) {
                //真正去验证 conditional 的逻辑判断是否应该跳过
                skip = conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
            }
            //写入缓存
            this.skipped.put(configClass, skip);

            return skip;
        }
    }


}
