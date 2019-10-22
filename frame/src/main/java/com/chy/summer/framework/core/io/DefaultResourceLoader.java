package com.chy.summer.framework.core.io;

import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.io.support.ResourceUtils;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;

import java.net.MalformedURLException;
import java.net.URL;

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
        Assert.notNull(location, "文件位置不能 为空");


        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
        }
        else {
            URL url = null;
            try {
                url = new URL(location);
            } catch (MalformedURLException e) {

            }
            return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
