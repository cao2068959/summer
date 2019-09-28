package com.chy.summer.framework.core.type.filter;

import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

/**
 * 用于解析 MetadataReader判断是否是符合的元数据类型
 */
public interface TypeFilter {

    boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
            throws IOException;
}
