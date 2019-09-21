package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.annotation.constant.AnnotationConstant;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.*;


/**
 * 用来解析注解的一些数据,以及注解的所有继承信息
 */
public class MetadataAnnotationVisitorHandle extends AnnotationVisitor  {
    /**
     * 这2个属性同 ClassMetadataReadingVisitor 中的,只是传过来设置值而已
     */
    private final Map<String, AnnotationAttributes> attributesMap;

    private final Map<String, Set<String>> metaAnnotationMap;

    private final String className;

    private  Class<? extends Annotation> annotationType;

    public MetadataAnnotationVisitorHandle(String className, Map<String, AnnotationAttributes> attributesMap
            , Map<String, Set<String>> metaAnnotationMap) {

        super(Opcodes.ASM5);
        this.attributesMap = attributesMap;
        this.metaAnnotationMap = metaAnnotationMap;
        this.className = className;
    }

    @Override
    public void visit(String s, Object o) {
        System.out.println(s);
    }

    @Override
    public void visitEnd() {
         annotationType = getAnnotationType();
    }


    /**
     * 解析注解的继承关系，把他的继承关系存入 metaAnnotationMap
     */
    private void metaAnnotationMapHandle(Class<? extends Annotation> annotationType,Set<String> result){
        if(result == null){
            result = new LinkedHashSet<>();
        }
        //获取了这个注解上面的父注解
        Annotation[] annotations = annotationType.getAnnotations();
        Set<String> finalResult = result;
        Arrays.stream(annotations).filter(annotation -> {
           return !AnnotationConstant.ignoreAnnotation.contains(annotation.getClass());
        }).forEach(annotation ->{
            metaAnnotationMapHandle(annotation.getClass(), finalResult);
        } );

    }




    /**
     * 获取className 对应路径的注解，有缓存
     */
    private Class<? extends Annotation> getAnnotationType(){
        if(this.annotationType != null){
            return this.annotationType;
        }
        this.annotationType = getAnnotationType(className);
        return this.annotationType;
    }

    /**
     * 通过注解的路径拿到注解
     */
    private Class<? extends Annotation> getAnnotationType(String path){
        if(StringUtils.isEmpty(path)){
            throw new RuntimeException("获取注解的时候路径不能为Null");
        }
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        try {
            return (Class<? extends Annotation>)defaultClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
