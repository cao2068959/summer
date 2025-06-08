package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.annotation.AnnotationAttributeHolder;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.exception.AsmException;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;
import lombok.Setter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 用来解析注解的一些数据,以及注解的所有继承信息
 */
public class MetadataAnnotationVisitorHandle extends AnnotationVisitor {

    private final String className;

    private Class<? extends Annotation> annotationType;

    /**
     * 是否是在执行数组的数据采集,如果是这里将不会执行 visitEnd
     * 不等于 Null 则是数组
     */
    @Setter
    private String arraykey;

    /**
     * 这个注解上的属性
     */
    @Setter
    private  Map<String, Object> annotationAttributes = new HashMap<>();

    private Map<String, AnnotationAttributeHolder> ownAllAnnotated;

    private Set<String> ownAllAnnotatedType;


    public MetadataAnnotationVisitorHandle(String className, Map<String, AnnotationAttributeHolder> ownAllAnnotated,
                                           Set<String> ownAllAnnotatedType) {

        super(Opcodes.ASM5);
        this.className = className;
        this.ownAllAnnotated = ownAllAnnotated;
        this.ownAllAnnotatedType = ownAllAnnotatedType;
    }


    /**
     * 注解里每有一个方法,就会调用一次
     */
    @Override
    public void visit(String key, Object value) {
        //先处理一下value的类型
        //如果传入的是一个 class类型,那么把他转成 string类型 ,存入的是这个类的全路
        if (value instanceof Type) {
            value = ((Type) value).getClassName();
        }

        //然后在处理一下 value的值
        key = key == null ? arraykey : key;

        Object annotationAttribute = annotationAttributes.get(key);
        //如果已经存在了对应的值,那么考虑是否是数组类型
        if (arraykey != null || annotationAttribute != null) {
            //如果是数组,把值添加到数组里面
            value = ObjectUtils.addObjectToArray((Object[]) annotationAttribute, value);
        }
        annotationAttributes.put(key, value);
    }

    /**
     * 如果是数组,会递归再次进入该 MetadataAnnotationVisitorHandle , 但是不会去执行visitEnd
     *
     * @return
     */
    @Override
    public AnnotationVisitor visitArray(String key) {
        MetadataAnnotationVisitorHandle visitorHandle = new MetadataAnnotationVisitorHandle(className, ownAllAnnotated, ownAllAnnotatedType);
        visitorHandle.setArraykey(key);
        visitorHandle.setAnnotationAttributes(annotationAttributes);
        return visitorHandle;
    }

    @Override
    public void visitEnum(String key, String typeResource, String value) {
        String typeClassName = ClassUtils.convertResourcePathToClassName(typeResource);
        Class<? extends Enum> typeClass = null;
        Object currentValue = null;
        try {
            typeClass = (Class<? extends Enum>) ClassUtils.forNameCache(typeClassName);
            Method valueOf = typeClass.getMethod("valueOf", String.class);
            currentValue = valueOf.invoke(null, value);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new AsmException("类型 [%s] 没有找到", typeResource);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new AsmException("反射生成类型 [%s] 失败", typeClass);
        }
        visit(key, currentValue);
    }


    /**
     * 当注解全部的钩子全部触发后才会调用
     */
    @Override
    public void visitEnd() {
        if (arraykey != null) {
            return;
        }
        annotationType = getAnnotationType();
        //这个注解上面还有哪一些继承上去的注解
        AnnotationAttributeHolder holder = AnnotationUtils.metaAnnotationMapHandle(annotationType, annotationAttributes, null);
        ownAllAnnotated.put(holder.getName(), holder);
        ownAllAnnotatedType.addAll(holder.getContain());
        //执行注解上的继承任务,把所有的继承属性给赋值过去
        AnnotationUtils.doAliasForTask(holder);
    }


    /**
     * 获取className 对应路径的注解，有缓存
     */
    private Class<? extends Annotation> getAnnotationType() {
        if (this.annotationType != null) {
            return this.annotationType;
        }
        this.annotationType = getAnnotationType(className);
        return this.annotationType;
    }

    /**
     * 通过注解的路径拿到注解
     */
    private Class<? extends Annotation> getAnnotationType(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new RuntimeException("获取注解的时候路径不能为Null");
        }
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        try {
            return (Class<? extends Annotation>) defaultClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
