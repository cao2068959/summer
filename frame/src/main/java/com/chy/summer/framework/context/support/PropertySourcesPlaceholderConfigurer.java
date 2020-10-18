package com.chy.summer.framework.context.support;


import com.chy.summer.framework.beans.config.BeanFactoryPostProcessor;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.context.EnvironmentAware;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.evn.propertysource.MutablePropertySources;
import com.chy.summer.framework.core.evn.propertysource.PropertySource;
import com.chy.summer.framework.core.evn.resolver.ConfigurablePropertyResolver;
import com.chy.summer.framework.core.evn.resolver.PropertySourcesPropertyResolver;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.StringValueResolver;


public class PropertySourcesPlaceholderConfigurer implements BeanFactoryPostProcessor, EnvironmentAware {

    public static final String ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties";


    private MutablePropertySources propertySources;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.propertySources == null) {
            this.propertySources = new MutablePropertySources();
            if (this.environment != null) {
                this.propertySources.addLast(
                        new PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, this.environment) {
                            @Override
                            public String getProperty(String key) {
                                return this.source.getProperty(key);
                            }
                        }
                );
            }
        }
        processProperties(beanFactory, new PropertySourcesPropertyResolver(this.propertySources));
    }


    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     final ConfigurablePropertyResolver propertyResolver) throws BeansException {

        StringValueResolver valueResolver = strVal -> {
            String resolved = propertyResolver.resolveRequiredPlaceholders(strVal);
            return resolved;
        };

        doProcessProperties(beanFactoryToProcess, valueResolver);
    }


    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       StringValueResolver valueResolver) {
        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
    }
}
