package com.chy.summer.framework.boot.autoconfigure;


import com.chy.summer.framework.beans.Aware;
import com.chy.summer.framework.beans.BeanClassLoaderAware;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.context.EnvironmentAware;
import com.chy.summer.framework.context.imported.deferred.DeferredImportSelector;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.support.SummerFactoriesLoader;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import lombok.Getter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware,
        BeanFactoryAware, EnvironmentAware, Ordered {

    @Getter
    private Environment environment;

    @Getter
    private BeanFactory beanFactory;

    @Getter
    private ClassLoader classLoader;

    private static final String[] NO_IMPORTS = {};
    private static final AutoConfigurationEntry EMPTY_ENTRY = new AutoConfigurationEntry();
    private ConfigurationClassFilter configurationClassFilter;


    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
            return NO_IMPORTS;
        }
        AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
    }

    protected boolean isEnabled(AnnotationMetadata metadata) {
        if (getClass() == AutoConfigurationImportSelector.class) {
            return getEnvironment().getProperty(EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY, Boolean.class, true);
        }
        return true;
    }


    protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
            return EMPTY_ENTRY;
        }
        //获取 @EnableAutoConfiguration 注解中的属性
        AnnotationAttributes attributes = getEnableAutoConfigurationAttributes(annotationMetadata);
        //去 META-INF/summer.factories 获取配置了 自动配置的bean
        List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
        //移除引用
        configurations = removeDuplicates(configurations);

        Set<String> exclusions = getExclusions(annotationMetadata, attributes);
        //移除收集到需要排除的类
        configurations.removeAll(exclusions);

        //这里是一个优化操作，提交把一些常用的自动配置类给放文件中，然后提前校验
        //TODO 先暂时不需要这个优化
        //configurations = getConfigurationClassFilter().filter(configurations);

        //执行对应的  AutoConfigurationImportListeners
        fireAutoConfigurationImportEvents(configurations, exclusions);

        return new AutoConfigurationEntry(configurations, exclusions);
    }

    private void fireAutoConfigurationImportEvents(List<String> configurations, Set<String> exclusions) {
        //从 summer.factories 文件中获取对应的  AutoConfigurationImportListeners 然后执行
        List<AutoConfigurationImportListener> listeners = getAutoConfigurationImportListeners();


        if (!listeners.isEmpty()) {
            AutoConfigurationImportEvent event = new AutoConfigurationImportEvent(this, configurations, exclusions);
            for (AutoConfigurationImportListener listener : listeners) {
                //给刚实例出来的 listener Bean  给执行对应的 aware 接口, 注入对应的实例
                invokeAwareMethods(listener);
                //执行真正的监听器执行接口
                listener.onAutoConfigurationImportEvent(event);
            }
        }
    }

    private void invokeAwareMethods(Object instance) {
        if (instance instanceof Aware) {
            if (instance instanceof BeanClassLoaderAware) {
                ((BeanClassLoaderAware) instance).setBeanClassLoader(this.classLoader);
            }
            if (instance instanceof BeanFactoryAware) {
                ((BeanFactoryAware) instance).setBeanFactory(this.beanFactory);
            }
            if (instance instanceof EnvironmentAware) {
                ((EnvironmentAware) instance).setEnvironment(this.environment);
            }
        }
    }

    protected List<AutoConfigurationImportListener> getAutoConfigurationImportListeners() {
        return SummerFactoriesLoader.loadFactories(AutoConfigurationImportListener.class, this.classLoader);
    }

    protected AnnotationAttributes getEnableAutoConfigurationAttributes(AnnotationMetadata metadata) {
        String name = EnableAutoConfiguration.class.getName();
        AnnotationAttributes attributes = metadata.getAnnotationAttributes(name);
        Assert.notNull(attributes, "在类[" + metadata.getClassName() + "] 中找不到对应的的 @EnableAutoConfiguration 注解");
        return attributes;
    }

    private ConfigurationClassFilter getConfigurationClassFilter() {
        return this.configurationClassFilter;
    }


    protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        Set<String> excluded = new LinkedHashSet<>();
        excluded.addAll(Arrays.stream(attributes.getRequiredAttribute("exclude", Class[].class))
                .map(Class::toString).collect(Collectors.toList()));

        excluded.addAll(Arrays.stream(attributes.getRequiredAttribute("excludeName", String[].class))
                .collect(Collectors.toList()));
        //除了注解中去获取要排除的自动配置类，还可以从配置属性中获取，这里先不处理
        //excluded.addAll(getExcludeAutoConfigurationsProperty());
        return excluded;
    }

    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        List<String> configurations = SummerFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, classLoader);
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
                + "are using a custom packaging, make sure that file is correct.");
        return configurations;
    }

    protected final <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    @Override
    public Predicate<String> getExclusionFilter() {
        return null;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    @Override
    public int getOrder() {
        return 0;
    }


}
