package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Set;


/**
 * 用了解析注解的一些数据,以及注解的所有继承信息
 */
public class MetadataAnnotationVisitorHandle extends AnnotationVisitor  {


    /**
     * 这2个属性同 ClassMetadataReadingVisitor 中的,只是传过来设置值而已
     */
    private final Map<String, AnnotationAttributes> attributesMap;

    private final Map<String, Set<String>> metaAnnotationMap;


    public MetadataAnnotationVisitorHandle( Map<String, AnnotationAttributes> attributesMap, Map<String, Set<String>> metaAnnotationMap) {
        super(Opcodes.ASM5);
        this.attributesMap = attributesMap;
        this.metaAnnotationMap = metaAnnotationMap;
    }





}
