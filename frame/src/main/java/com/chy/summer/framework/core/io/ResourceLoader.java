package com.chy.summer.framework.core.io;


import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.io.support.ResourceUtils;

public interface ResourceLoader {

    String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

    Resource getResource(String location);

    ClassLoader getClassLoader();
}
