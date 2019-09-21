package com.chy.summer.framework.core.annotation;

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


    class AnnotationAttribute{
        Object value;
        Object defaultValue;
    }

}
