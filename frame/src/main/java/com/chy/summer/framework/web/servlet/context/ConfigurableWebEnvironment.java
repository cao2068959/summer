package com.chy.summer.framework.web.servlet.context;

import com.chy.summer.framework.core.evn.ConfigurableEnvironment;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public interface ConfigurableWebEnvironment extends ConfigurableEnvironment {


    void initPropertySources(ServletContext servletContext, ServletConfig servletConfig);

}
