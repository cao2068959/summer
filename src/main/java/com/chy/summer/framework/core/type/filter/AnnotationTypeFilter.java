package com.chy.summer.framework.core.type.filter;

import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;

public class AnnotationTypeFilter implements TypeFilter {


    public AnnotationTypeFilter(Class<? extends Annotation> componentClass) {


    }


    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        return false;
    }
}
