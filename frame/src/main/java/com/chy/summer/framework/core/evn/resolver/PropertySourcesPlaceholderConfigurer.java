package com.chy.summer.framework.core.evn.resolver;


import com.chy.summer.framework.beans.config.BeanFactoryPostProcessor;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.beans.support.AbstractBeanFactory;
import com.chy.summer.framework.context.EnvironmentAware;
import com.chy.summer.framework.core.StringValueResolver;
import com.chy.summer.framework.core.evn.AbstractEnvironment;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.exception.BeansException;

public class PropertySourcesPlaceholderConfigurer implements BeanFactoryPostProcessor, EnvironmentAware {


    private Environment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        AbstractBeanFactory abstractBeanFactory;
        if (!(beanFactory instanceof AbstractBeanFactory) || !(environment instanceof AbstractEnvironment)) {
            return;
        }

        abstractBeanFactory = (AbstractBeanFactory) beanFactory;

        AbstractEnvironment abstractEnvironment = (AbstractEnvironment) environment;
        StringValueResolver valueResolver = (strVal) -> {
            String resolved = abstractEnvironment.getPropertyResolver().resolveRequiredPlaceholders(strVal);
            return resolved;
        };
        abstractBeanFactory.addEmbeddedValueResolvers(valueResolver);

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
