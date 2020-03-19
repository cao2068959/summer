package com.chy.summer.framework.core.evn;


import com.chy.summer.framework.core.evn.propertysource.MutablePropertySources;
import com.chy.summer.framework.core.evn.propertysource.PropertiesPropertySource;

public class StandardEnvironment extends AbstractEnvironment {

    public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";

    public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";


    /**
     * 把系统环境变量以及一些 JVM参数都给放进去
     * @param propertySources
     */
    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        propertySources.addLast(
                new PropertiesPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, getSystemProperties()));
        propertySources.addLast(
                new PropertiesPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, getSystemEnvironment()));
    }
}
