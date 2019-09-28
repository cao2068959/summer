package com.chy.summer.framework.core.io;

import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.ClassUtils;

/**
 * 默认的源文件加载器
 */
public class DefaultResourceLoader implements ResourceLoader {

    private ClassLoader classLoader;

    public DefaultResourceLoader() {
         this.classLoader = ClassUtils.getDefaultClassLoader();
    }

    @Override
    public Resource getResource(String location) {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
