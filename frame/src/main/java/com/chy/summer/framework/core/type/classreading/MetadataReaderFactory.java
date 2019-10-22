package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.io.support.ResourceUtils;

import java.io.IOException;

public interface MetadataReaderFactory {



    MetadataReader getMetadataReader(String className) throws IOException;


    MetadataReader getMetadataReader(Resource resource) throws IOException;

}
