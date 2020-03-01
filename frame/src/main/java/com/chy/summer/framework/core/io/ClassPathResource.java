package com.chy.summer.framework.core.io;

import com.chy.summer.framework.core.io.support.AbstractFileResolvingResource;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class ClassPathResource extends AbstractFileResolvingResource {

    @Getter
    private final String path;

    private ClassLoader classLoader;

    private Class<?> clazz;

    public ClassPathResource(String path, Class<?> clazz) {
        Assert.notNull(path, "path 不能为空");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
    }

    public ClassPathResource(String path,  ClassLoader classLoader) {
        Assert.notNull(path, "path 不能为空");
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }


    protected URL resolveURL() {
        if (this.clazz != null) {
            return this.clazz.getResource(this.path);
        }
        else if (this.classLoader != null) {
            return this.classLoader.getResource(this.path);
        }
        else {
            return ClassLoader.getSystemResource(this.path);
        }
    }


    @Override
    public boolean exists() {
        return (resolveURL() != null);
    }

    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return (this.clazz != null ? new ClassPathResource(pathToUse, this.clazz) :
                new ClassPathResource(pathToUse, this.classLoader));
    }


    @Override
    public URL getURL() throws IOException {
        URL url = resolveURL();
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }


    @Override
    public URI getURI() throws IOException {
        return null;
    }

    @Override
    public long contentLength() throws IOException {
        return 0;
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }



    @Override
    public String getFilename() {
        return StringUtils.getFilename(this.path);
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = this.path;
        if (this.clazz != null && !pathToUse.startsWith("/")) {
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        }
        else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        }
        else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " 不能打开 inputStream 因为对应的文件不存在");
        }
        return is;
    }
}
