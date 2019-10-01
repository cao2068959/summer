package com.chy.summer.framework.core.annotation;


import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public  abstract class AnnotationUtils {

    private static Map<String,List<Method>>  annotationMethodCache = new ConcurrentHashMap<>();

    private static final Map<AnnotationCacheKey, Annotation> findAnnotationCache =
            new ConcurrentHashMap<>(256);

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

        AnnotationAttributes attributes = new AnnotationAttributes(annotationClass.getName());
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
        Assert.notNull(clazz, "Class must not be null");
        if (annotationType == null) {
            return null;
        }

        /**
         * 缓存的key 类+注解 确定一个key
         */
        AnnotationCacheKey cacheKey = new AnnotationCacheKey(clazz, annotationType);
        A result = (A) findAnnotationCache.get(cacheKey);
        if (result == null) {
            result = findAnnotation(clazz, annotationType, new HashSet<>());
            if (result != null) {
                findAnnotationCache.put(cacheKey, result);
            }
        }
        return result;
    }

    private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType, Set<Annotation> visited) {
        try {
            //直接去拿类上的注解,拿到了就直接返回
            A annotation = clazz.getDeclaredAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
            //这里没直接拿到,递归判断,注解上的注解.
            for (Annotation declaredAnn : clazz.getDeclaredAnnotations()) {
                Class<? extends Annotation> declaredType = declaredAnn.annotationType();
                if (!isInJavaLangAnnotationPackage(declaredType) && visited.add(declaredAnn)) {
                    annotation = findAnnotation(declaredType, annotationType, visited);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        }
        catch (Throwable ex) {
            log.warn("获取类 [%s] 上的注解 [%s] 失败",clazz,annotationType);
            return null;
        }

        for (Class<?> ifc : clazz.getInterfaces()) {
            A annotation = findAnnotation(ifc, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return findAnnotation(superclass, annotationType, visited);
    }


    /**
     * 判断这个注解是不是 jdk 里面定义注解的那几个注解,比如 @Documented @Target
     * @param annotationType
     * @return
     */
    static boolean isInJavaLangAnnotationPackage(@Nullable Class<? extends Annotation> annotationType) {
        return (annotationType != null && annotationType.getName().startsWith("java.lang.annotation"));
    }


    /**
     * 在给定的方法上找到相应的注解，如果注解没有直接出现在给定方法上将遍历它的父方法或者父接口。
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(Method method, @Nullable Class<A> annotationType) {
        //TODO GYX 尚未实现
        return null;
    }



    private static final class AnnotationCacheKey implements Comparable<AnnotationCacheKey> {

        private final AnnotatedElement element;

        private final Class<? extends Annotation> annotationType;

        public AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
            this.element = element;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AnnotationCacheKey)) {
                return false;
            }
            AnnotationCacheKey otherKey = (AnnotationCacheKey) other;
            return (this.element.equals(otherKey.element) && this.annotationType.equals(otherKey.annotationType));
        }

        @Override
        public int hashCode() {
            return (this.element.hashCode() * 29 + this.annotationType.hashCode());
        }

        @Override
        public String toString() {
            return "@" + this.annotationType + " on " + this.element;
        }

        @Override
        public int compareTo(AnnotationCacheKey other) {
            int result = this.element.toString().compareTo(other.element.toString());
            if (result == 0) {
                result = this.annotationType.getName().compareTo(other.annotationType.getName());
            }
            return result;
        }
    }

}


