package com.chy.summer.framework.core.annotation;


import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public  abstract class AnnotationUtils {

    private static Map<String,List<Method>>  annotationMethodCache = new ConcurrentHashMap<>();

    /**
     * 把注解里的属性给抽出来放入 AnnotationAttributes
     * 如果这里有属性需要继承下去,就把相关任务放入 LinkList 中,这里是有顺序要求的
     * @annotationValue 这里根据不同的类型执行不同的策略,是注解类型就反射拿,是map类型就put拿
     * @annotationClass 要解析的注解的类型
     * @aliasForTaskList 如果属性上有打了注解 @AliasFor 则生成任务放入队列里
     */
    public static AnnotationAttributes pareAnnotationToAttributes(Object value,
                                                                   Class<? extends Annotation> annotationClass,
                                                                   List<AliasForTask> aliasForTaskList) {

        AnnotationAttributes attributes = new AnnotationAttributes();
        List<Method> methodsByCache = getMethodsByCache(annotationClass);
        methodsByCache.stream().forEach(method -> {

            //通过value的类型拿到真正的 annotationValue
            Object annotationValue = getAnnotationValue(value,method);

            Object defaultValue = method.getDefaultValue();
            attributes.put(method.getName(),annotationValue,defaultValue);
            //判断上这个属性上是否有 @AliasFor 注解,有的话这个方法上的值将会传递给对应的注解上
            AliasFor aliasFor = method.getAnnotation(AliasFor.class);
            if(aliasFor == null){return;}
            aliasForTaskList.add(new AliasForTask(aliasFor,method.getName(),annotationClass));
        });
        return attributes;
    }


    private static Object getAnnotationValue(Object value,Method method){
        if(value instanceof Annotation){
          return  getAnnotationValueByAnnotation((Annotation) value,method);
        }

        if(value instanceof Map){
           return getAnnotationValueByMap((Map<String, Object>) value,method);
        }



        return null;
    }


    private static Object getAnnotationValueByMap(Map<String,Object> map,Method method){
        String name = method.getName();
        return map.get(name);
    }

    private static Object getAnnotationValueByAnnotation(Annotation annotation,Method method){
        Object annotationValue = null;
        try {
            //获取注解里的属性值
            annotationValue = method.invoke(annotation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return annotationValue;
    }



    /**
     * 获取注解里所有方法,这里会先过滤,然后有缓存
     * @return
     */
    static List<Method> getMethodsByCache(Class<? extends Annotation> annotationClass){
        List<Method> result = annotationMethodCache.get(annotationClass.getName());
        if(result == null){
            Method[] methods = annotationClass.getDeclaredMethods();
            result = new ArrayList<>(methods.length);
            final List fresult = result;
            Arrays.stream(methods).filter(AnnotationUtils::isAttributeMethod)
                    .forEach(method -> {
                        method.setAccessible(true);
                        fresult.add(method);
                    });
            annotationMethodCache.put(annotationClass.getName(),result);
        }

        return result;

    }


    /**
     * 判断 注解里的某个方法是不是属性
     * @param method
     * @return
     */
    static boolean isAttributeMethod( Method method) {
        return (method != null && method.getParameterCount() == 0 && method.getReturnType() != void.class);
    }

    /**
     * 在给定的类上找到相应的注解，遍历其接口、注解，如果注解没有直接出现在给定类本身上将遍历它的父类。
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        //TODO GYX 尚未实现
        return null;
    }

    /**
     * 在给定的方法上找到相应的注解，如果注解没有直接出现在给定方法上将遍历它的父方法或者父接口。
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(Method method, @Nullable Class<A> annotationType) {
        //TODO GYX 尚未实现
        return null;
    }



}


