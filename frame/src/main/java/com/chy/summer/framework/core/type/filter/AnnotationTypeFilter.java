package com.chy.summer.framework.core.type.filter;

import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;

public class AnnotationTypeFilter implements TypeFilter {


    private final Class<? extends Annotation> annotationType;

    public AnnotationTypeFilter(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;

    }


    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {

        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        if(annotationMetadata.hasAnnotation(annotationType.getName())){
            return true;
        }

        if(annotationMetadata.hasMetaAnnotation(annotationType.getName())){
            return true;
        }

        return false;
    }
}
