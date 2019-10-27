package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.ClassUtils;
import jdk.internal.org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 元数据读取器工厂
 */
public class DefaultMetadataReaderFactory implements MetadataReaderFactory {

    private  ResourceLoader resourceLoader;

    public DefaultMetadataReaderFactory() {
        this(null);
    }

    public DefaultMetadataReaderFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public MetadataReader getMetadataReader(String className) throws IOException {
        try {
            //变成 classpath:xxx 的形式d
            String resourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(className) + ClassUtils.CLASS_FILE_SUFFIX;
            Resource resource = this.resourceLoader.getResource(resourcePath);
            return getMetadataReader(resource);
        }
        catch (FileNotFoundException ex) {
            //这里报错除了 是找不类可能还是因为 是 内部类的原因,所以这里 会把 $ 后面的解析,去扫描内部类
            int lastDotIndex = className.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String innerClassName =
                        className.substring(0, lastDotIndex) + '$' + className.substring(lastDotIndex + 1);
                String innerClassResourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(innerClassName) + ClassUtils.CLASS_FILE_SUFFIX;
                Resource innerClassResource = this.resourceLoader.getResource(innerClassResourcePath);
                if (innerClassResource.exists()) {
                    return getMetadataReader(innerClassResource);
                }
            }
            throw ex;
        }
    }

    /**
     * 通过一个class文件的地址，用asm去解析这个class里面对应的
     * 类信息，或者注解信息，避免了把大量无用的class装载到jvm中
     *
     * @param resource
     * @return
     * @throws IOException
     */
    @Override
    public MetadataReader getMetadataReader(Resource resource) throws IOException {
        InputStream is = new BufferedInputStream(resource.getInputStream());
        ClassReader classReader;

        try {
            classReader = new ClassReader(is);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException();
        } finally {
            is.close();
        }
        ClassMetadataReadingVisitor visitor = new ClassMetadataReadingVisitor();
        classReader.accept(visitor, ClassReader.SKIP_DEBUG);
        return new SimpleMetadataReader(visitor, visitor, resource);
    }
}
