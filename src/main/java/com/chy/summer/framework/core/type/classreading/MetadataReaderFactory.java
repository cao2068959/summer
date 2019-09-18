package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.support.Resource;

import java.io.IOException;

public interface MetadataReaderFactory {


    MetadataReader getMetadataReader(String className) throws IOException;


    MetadataReader getMetadataReader(Resource resource) throws IOException;

}
