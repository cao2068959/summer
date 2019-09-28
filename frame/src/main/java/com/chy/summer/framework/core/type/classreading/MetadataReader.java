package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.ClassMetadata;

public interface MetadataReader {


    Resource getResource();


    ClassMetadata getClassMetadata();


    AnnotationMetadata getAnnotationMetadata();

}
