package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.util.Assert;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个注解里的所有属性值都会放入这个类
 */
public class AnnotationAttributes {

    private Map<String,AnnotationAttribute> datas = new HashMap<>();

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
        if(annotationAttribute == null){
            throw new RuntimeException(key+" 属性不存在");
        }
        if(annotationAttribute.value == null){
            return annotationAttribute.defaultValue;
        }
        return annotationAttribute.value;
    }


    private <T> T getRequiredAttribute(String attributeName, Class<T> expectedType) {
        //TODO 电脑没电了留坑
        Object value = datas.get(attributeName);
        assertAttributePresence(attributeName, value);
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
                attributeName, this.displayName));
    }

    private void assertAttributeType(String attributeName, Object attributeValue, Class<?> expectedType) {
        if (!expectedType.isInstance(attributeValue)) {
            throw new IllegalArgumentException(String.format(
                    "Attribute '%s' is of type [%s], but [%s] was expected in attributes for annotation [%s]",
                    attributeName, attributeValue.getClass().getSimpleName(), expectedType.getSimpleName(),
                    this.displayName));
        }
    }


    class AnnotationAttribute{
        Object value;
        Object defaultValue;
    }

}
