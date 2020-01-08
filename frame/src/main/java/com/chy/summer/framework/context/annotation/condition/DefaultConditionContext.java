package com.chy.summer.framework.context.annotation.condition;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.context.ConfigurableApplicationContext;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.util.ClassUtils;
import lombok.Getter;


/**
 *  ConditionContext 接口的默认实现
 */
public class DefaultConditionContext implements ConditionContext {

    @Getter
    private final BeanDefinitionRegistry registry;

    @Getter
    private final ConfigurableListableBeanFactory beanFactory;

    @Getter
    private final Environment environment;

    @Getter
    private final ResourceLoader resourceLoader;

    @Getter
    private final ClassLoader classLoader;

    public DefaultConditionContext(BeanDefinitionRegistry registry,
                                Environment environment, ResourceLoader resourceLoader) {

        this.registry = registry;
        this.beanFactory = deduceBeanFactory(registry);
        this.environment = environment;
        this.resourceLoader = (resourceLoader != null ? resourceLoader : deduceResourceLoader(registry));
        this.classLoader = deduceClassLoader(resourceLoader, this.beanFactory);
    }

    private ConfigurableListableBeanFactory deduceBeanFactory(BeanDefinitionRegistry source) {
        if (source instanceof ConfigurableListableBeanFactory) {
            return (ConfigurableListableBeanFactory) source;
        }
        if (source instanceof ConfigurableApplicationContext) {
            return (((ConfigurableApplicationContext) source).getBeanFactory());
        }
        return null;
    }



    private ResourceLoader deduceResourceLoader(BeanDefinitionRegistry source) {
        if (source instanceof ResourceLoader) {
            return (ResourceLoader) source;
        }
        return new DefaultResourceLoader();
    }

    private ClassLoader deduceClassLoader(ResourceLoader resourceLoader,
                                          ConfigurableListableBeanFactory beanFactory) {

        if (resourceLoader != null) {
            ClassLoader classLoader = resourceLoader.getClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }
        if (beanFactory != null) {
            return beanFactory.getBeanClassLoader();
        }
        return ClassUtils.getDefaultClassLoader();
    }

}
