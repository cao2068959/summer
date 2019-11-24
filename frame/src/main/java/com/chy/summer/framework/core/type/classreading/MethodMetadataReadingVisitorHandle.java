package com.chy.summer.framework.core.type.classreading;

import aj.org.objectweb.asm.Opcodes;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.type.MethodMetadata;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  asm 对于方法的解析处理类,同时 这个类也实现 MethodMetadata 当做容器类
 *  一个本类的对象代表了一个方法
 */
public class MethodMetadataReadingVisitorHandle extends MethodVisitor implements MethodMetadata {


    private final String methodName;
    private final int access;
    private final String className;
    private final String returnTypeName;
    /** 这个类上的所有方法的对象放这里面, 这当然也是外面传进来的 */
    private final Set<MethodMetadata> methodMetadataSet;
    /** 这个方法上面所有的注解  key : 注解的name value: 这个注解上面的所有派生注解  */
    private final Map<String, Set<String>> metaAnnotationMap;
    /** 这个方法上所有注解的属性的集合 */
    private final Map<String, AnnotationAttributes> annotationAttributesMap = new HashMap<>();



    public MethodMetadataReadingVisitorHandle(String methodName, int access, String className, String returnTypeName, Set<MethodMetadata> methodMetadataSet) {
        super(Opcodes.ASM5);
        this.methodName = methodName;
        this.access = access;
        this.className = className;
        this.returnTypeName = returnTypeName;
        this.methodMetadataSet = methodMetadataSet;
        metaAnnotationMap = new HashMap<>();
        this.methodMetadataSet.add(this);
    }

    /**
     * 方法上每有一个注解就会调用一次这个方法
     * @param desc
     * @param visible
     * @return
     */
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
        String className = Type.getType(desc).getClassName();
        return new MetadataAnnotationVisitorHandle(className, this.annotationAttributesMap, this.metaAnnotationMap);
    }



    //======================================下面是 MethodMetadata 接口的实现==============================================

    /**
     * 查询这个方法上有没有指定的注解
     * @param annotationName
     * @return
     */
    @Override
    public boolean isAnnotated(String annotationName) {
        //先扫描方法上面有没有指定的注解
        if(metaAnnotationMap.containsKey(annotationName)){
            return true;
        }
        //如果没有就去看看派生注解里有没有
        for (Set<String> value : metaAnnotationMap.values()) {
            if(value.contains(annotationName)){
                return true;
            }
        }
        //如果全部都没有 就返回false
        return false;
    }


    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getDeclaringClassName() {
        return className;
    }

    @Override
    public String getReturnTypeName() {
        return returnTypeName;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isOverridable() {
        return false;
    }



    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName) {
        return annotationAttributesMap.get(annotationName);
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return null;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type) {
        return null;
    }

    @Override
    public boolean hasMetaAnnotation(String annotationName) {
        return false;
    }

    @Override
    public boolean hasAnnotation(String metaAnnotationName) {
        return false;
    }


}
