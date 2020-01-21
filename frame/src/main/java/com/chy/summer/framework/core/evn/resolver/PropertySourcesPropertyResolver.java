package com.chy.summer.framework.core.evn.resolver;


import com.chy.summer.framework.core.evn.PropertySources;

public class PropertySourcesPropertyResolver implements ConfigurablePropertyResolver {


    private final PropertySources propertySources;

    public PropertySourcesPropertyResolver(PropertySources propertySources) {
        this.propertySources = propertySources;
    }


}
