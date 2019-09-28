package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.ClassMetadata;

public class SimpleMetadataReader implements MetadataReader {

    private AnnotationMetadata annotationMetadata;
    private ClassMetadata classMetadata;
    private Resource resource;

    public SimpleMetadataReader(AnnotationMetadata annotationMetadata, ClassMetadata classMetadata, Resource resource) {
        this.annotationMetadata = annotationMetadata;
        this.classMetadata = classMetadata;
        this.resource = resource;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public ClassMetadata getClassMetadata() {
        return classMetadata;
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return annotationMetadata;
    }
}
