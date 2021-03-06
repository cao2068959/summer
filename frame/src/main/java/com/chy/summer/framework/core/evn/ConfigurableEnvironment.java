package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.core.evn.propertysource.MutablePropertySources;

import java.util.Map;

public interface ConfigurableEnvironment extends Environment {

    void setActiveProfiles(String... profiles);

    void addActiveProfile(String profile);

    void setDefaultProfiles(String... profiles);

    Map<String, Object> getSystemEnvironment();

    Map<String, Object> getSystemProperties();

    void merge(ConfigurableEnvironment parent);

    MutablePropertySources getPropertySources();

}
