package com.chy.summer.framework.core.io;


import com.chy.summer.framework.core.io.support.Resource;

public interface ResourceLoader {

    Resource getResource(String location);

    ClassLoader getClassLoader();
}
