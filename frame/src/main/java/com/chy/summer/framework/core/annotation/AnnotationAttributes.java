package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 一个注解里的所有属性值都会放入这个类
 */
public class AnnotationAttributes {

    private final String className;
    private Map<String,AnnotationAttribute> datas = new HashMap<>();

    private String classSuffix = "#class";




    public AnnotationAttributes(String name) {
        this.className = name;
    }

    public AnnotationAttributes(AnnotationAttributes original) {
        this.className = original.getClassName();
        this.datas = original.getDatas();
    }

    public void put(String key,Object data,Object defaultValue){
        AnnotationAttribute annotationAttribute = new AnnotationAttribute();
        annotationAttribute.value = data;
        annotationAttribute.defaultValue = defaultValue;
        datas.put(key,annotationAttribute);
    }


    public void update(String key,Object data){
        AnnotationAttribute annotationAttribute = datas.get(key);
        if(annotationAttribute == null){
            throw new RuntimeException(key+" 属性不存在");
        }
        annotationAttribute.value = data;
    }

    /**
     * 获取属性的值,如果属性没有值则获取默认值
     * class类型的数据全部都是以 string类型存的class的全路径,这里如果用 key#class 来获取真实的class属性
     * 如果是正常的key拿到的是 string类型的 class全路径
     *
     * @param key
     */
    public Object getAttributeValue(String key){
        AnnotationAttribute annotationAttribute = datas.get(key);
        //如果没有拿到值,并且后缀是 #class 说明他是要拿 class类型,这边帮他生成对应的数据
        if(annotationAttribute == null && key.endsWith(classSuffix)){
            try {
                annotationAttribute = createClassFromPathString(key);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        //检查annotationAttribute 是否是Null，是的话直接抛出异常
        assertAttributePresence(key,annotationAttribute);
        if(annotationAttribute.value == null){
            return annotationAttribute.defaultValue;
        }
        return annotationAttribute.value;
    }

    /**
     * 使用 classPath 生成 对应的 class类型,并且存入到属性里
     * @param key
     * @return
     */
    private AnnotationAttribute createClassFromPathString(String key) throws ClassNotFoundException {
        String classPathKey = key.substring(0,key.lastIndexOf(classSuffix));
        Object classValue = getAttributeValue(classPathKey);
        AnnotationAttribute result = new AnnotationAttribute();
        if(classValue instanceof String){
            Class<?> resultClass = ClassUtils.forName((String) classValue, ClassUtils.getDefaultClassLoader());
            result.value = resultClass;
        }else if(classValue instanceof String[]){
            String[] classValues = (String[]) classValue;
            result.value = Arrays.stream(classValues).map(path -> {
                try {
                    return ClassUtils.forName((String) path, ClassUtils.getDefaultClassLoader());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }).toArray(Class[]::new);

        }else {
            throw new IllegalArgumentException("classValue 类型只能是 string或者 String[] 现在却是: "+classValue.getClass());
        }
        return result;
    }

    /**
     * 根据属性的name获取值.
     * @param attributeName 要获取的属性的名字
     * @param expectedType 要获取的类型
     */
    public <T> T getRequiredAttribute(String attributeName, Class<T> expectedType) {
        if(expectedType == Class.class || expectedType == Class[].class){
            attributeName = attributeName + classSuffix;
        }

        Object value = getAttributeValue(attributeName);
        if (!expectedType.isInstance(value) && expectedType.isArray() &&
                expectedType.getComponentType().isInstance(value)) {
            Object array = Array.newInstance(expectedType.getComponentType(), 1);
            Array.set(array, 0, value);
            value = array;
        }

        assertAttributeType(attributeName, value, expectedType);
        return (T) value;
    }

    public Boolean containsKey(String key){
        return datas.containsKey(key);
    }

    private void assertAttributePresence(String attributeName, Object attributeValue) {
        Assert.notNull(attributeValue, () -> String.format(
                "Attribute '%s' not found in attributes for annotation [%s]",
                attributeName, this.className));
    }

    private void assertAttributeType(String attributeName, Object attributeValue, Class<?> expectedType) {
        if (!expectedType.isInstance(attributeValue)) {
            throw new IllegalArgumentException(String.format(
                    "Attribute '%s' is of type [%s], but [%s] was expected in attributes for annotation [%s]",
                    attributeName, attributeValue.getClass().getSimpleName(), expectedType.getSimpleName(),
                    this.className));
        }
    }

    class AnnotationAttribute{
        Object value;
        Object defaultValue;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, AnnotationAttribute> getDatas() {
        return datas;
    }

    public void setDatas(Map<String, AnnotationAttribute> datas) {
        this.datas = datas;
    }
}
