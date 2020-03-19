package com.chy.summer.framework.web.servlet.context.support;

import com.chy.summer.framework.core.evn.StandardEnvironment;
import com.chy.summer.framework.core.evn.propertysource.MutablePropertySources;
import com.chy.summer.framework.core.evn.propertysource.StubPropertySource;

public class StandardServletEnvironment  extends StandardEnvironment {

    public static final String SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams";

    public static final String SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams";

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_PROPERTY_SOURCE_NAME));
        propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
        super.customizePropertySources(propertySources);
    }

}
