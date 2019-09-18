package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.ClassUtils;
import jdk.internal.org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultMetadataReaderFactory implements MetadataReaderFactory {


    private ClassMetadataReadingVisitor annotationMetadata;
    private ClassMetadataReadingVisitor classMetadata;
    private Resource resource;

    @Override
    public MetadataReader getMetadataReader(String className) throws IOException {
        return null;
    }

    /**
     * 用 asm 来解析 注解
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
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException();
        }
        finally {
            is.close();
        }

        ClassMetadataReadingVisitor visitor = new ClassMetadataReadingVisitor();
        classReader.accept(visitor, ClassReader.SKIP_DEBUG);

        MetadataReader result = new SimpleMetadataReader(null,visitor,resource);

        this.annotationMetadata = visitor;
        this.classMetadata = visitor;
        this.resource = resource;


        return result;
    }
}
