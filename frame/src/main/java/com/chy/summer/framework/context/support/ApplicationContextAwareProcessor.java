package com.chy.summer.framework.context.support;

import com.chy.summer.framework.beans.Aware;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.context.ApplicationContextAware;
import com.chy.summer.framework.context.ConfigurableApplicationContext;
import com.chy.summer.framework.context.EnvironmentAware;
import com.chy.summer.framework.exception.BeansException;

/**
 * 帮助一些需要的bean去注入一些 ApplicationContext 层级的对象，比如EnvironmentAware
 * d
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

    private final ConfigurableApplicationContext applicationContext;

    public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Aware) {
            invokeAwareInterfaces(bean);
        }
        return bean;
    }

    /**
     * 执行 Aware的接口
     *
     * @param bean
     */
    private void invokeAwareInterfaces(Object bean) {
        if (bean instanceof EnvironmentAware) {
            ((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
        }
    }

}
