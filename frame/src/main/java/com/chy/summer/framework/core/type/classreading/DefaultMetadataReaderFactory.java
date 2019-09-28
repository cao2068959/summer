package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.ClassUtils;
import jdk.internal.org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 元数据读取器工厂
 */
public class DefaultMetadataReaderFactory implements MetadataReaderFactory {


    @Override
    public MetadataReader getMetadataReader(String className) throws IOException {
        return null;
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
