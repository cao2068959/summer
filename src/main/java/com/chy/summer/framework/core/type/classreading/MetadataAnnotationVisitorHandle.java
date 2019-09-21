package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.annotation.constant.AnnotationConstant;
import com.chy.summer.framework.core.annotation.AliasForTask;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.*;


/**
 * 用了解析注解的一些数据,以及注解的所有继承信息
 */
public class MetadataAnnotationVisitorHandle extends AnnotationVisitor  {
    /**
     * 这2个属性同 ClassMetadataReadingVisitor 中的,只是传过来设置值而已
     */
    private final Map<String, AnnotationAttributes> attributesMap;

    private final Map<String, Set<String>> metaAnnotationMap;

    private final String className;

    private  Class<? extends Annotation> annotationType;

    /**
     * 如果注解的属性有继承关系,会把任务放这里面,全部初始化完成后,会执行继承任务,来把子注解的属性值传递给父注解
     */
    List<AliasForTask> aliasForTaskList = new LinkedList<>();

    /**
     * 这个注解上的属性
     */
    private final Map<String,Object> annotationAttributes = new HashMap<>();

    public MetadataAnnotationVisitorHandle(String className, Map<String, AnnotationAttributes> attributesMap
            , Map<String, Set<String>> metaAnnotationMap) {

        super(Opcodes.ASM5);
        this.attributesMap = attributesMap;
        this.metaAnnotationMap = metaAnnotationMap;
        this.className = className;
    }


    /**
     * 注解里每有一个方法,就会调用一次
     */
    @Override
    public void visit(String key, Object value) {
        annotationAttributes.put(key,value);
    }


    /**
     * 当注解全部的钩子全部触发后才会调用
     */
    @Override
    public void visitEnd() {
        annotationType = getAnnotationType();
        //这个注解上面还有哪一些继承上去的注解
        Set<String> allAnnotation = metaAnnotationMapHandle(annotationType, null);
        metaAnnotationMap.put(annotationType.getName(),allAnnotation);

        doAliasForTask();
    }

    /**
     * 执行注解属性继承关系的任务
     */
    private void doAliasForTask(){
        for (AliasForTask aliasForTask : aliasForTaskList) {

            AnnotationAttributes targetAttributes = attributesMap.get(aliasForTask.getTargerClass().getName());
            AnnotationAttributes formAttributes = attributesMap.get(aliasForTask.getFormClass().getName());

            targetAttributes.update(aliasForTask.getTargerName(),
                    formAttributes.getAttributeValue(aliasForTask.getFormName()));

        }

    }



    /**
     * 解析注解的继承关系，把他的继承关系存入 metaAnnotationMap
     */
    private Set<String> metaAnnotationMapHandle(Class<? extends Annotation> annotationType,Set<String> result){
        if(result == null){
            //这是根目录进来
            result = new LinkedHashSet<>();
            //解析root注解上面的属性
            AnnotationAttributes attributes = AnnotationUtils
                    .pareAnnotationToAttributes(annotationAttributes,annotationType,aliasForTaskList);
            attributesMap.put(annotationType.getName(),attributes);
        }else{
            result.add(annotationType.getName());
        }

        //获取了这个注解上面的父注解
        Annotation[] annotations = annotationType.getAnnotations();
        Set<String> finalResult = result;
        //递归扫描注解上面的注解,同时过滤掉一些不需要解析的注解
        Arrays.stream(annotations).filter(annotation -> {
           return !AnnotationConstant.ignoreAnnotation.contains(annotation.annotationType());
        }).forEach(annotation ->{
            Class<? extends Annotation> type = annotation.annotationType();
            //解析注解上面的属性
            AnnotationAttributes attributes = AnnotationUtils.pareAnnotationToAttributes(annotation,type,aliasForTaskList);
            metaAnnotationMapHandle(type, finalResult);
            attributesMap.put(type.getName(),attributes);
        });

        return result;

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
