package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.util.Assert;
import javafx.beans.binding.ObjectExpression;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个注解里的所有属性值都会放入这个类
 */
public class AnnotationAttributes {

    private final String className;
    private Map<String,AnnotationAttribute> datas = new HashMap<>();

    public AnnotationAttributes(String name) {
        this.className = name;
    }

    public void put(String key,Object data,Object defaultValue){
        AnnotationAttribute annotationAttribute = new AnnotationAttribute();
        annotationAttribute.value = data;
        annotationAttribute.defaultValue = defaultValue;
        this.datas.put(key,annotationAttribute);
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
     * @param key
     */
    public Object getAttributeValue(String key){
        AnnotationAttribute annotationAttribute = datas.get(key);
        //检查annotationAttribute 是否是Null，是的话直接抛出异常
        assertAttributePresence(key,annotationAttribute);
        if(annotationAttribute.value == null){
            return annotationAttribute.defaultValue;
        }
        return annotationAttribute.value;
    }

    /**
     * 根据属性的name获取值.
     * @param attributeName 要获取的属性的名字
     * @param expectedType 要获取的类型
     */
    public <T> T getRequiredAttribute(String attributeName, Class<T> expectedType) {
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

}
