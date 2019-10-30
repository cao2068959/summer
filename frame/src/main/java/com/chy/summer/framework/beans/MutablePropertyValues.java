package com.chy.summer.framework.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  PropertyValues 的实现类，代表了大量属性的集合
 *  一般放在 beanDefintion 里面，这样就代表了一个类上的所有属性
 */
public class MutablePropertyValues implements PropertyValues {

    private final Map<String,PropertyValue> propertyValueMap;

    private Set<String> processedProperties;

    public MutablePropertyValues() {
        this.propertyValueMap = new HashMap<>();
    }

    @Override
    public PropertyValue[] getPropertyValues() {
        return propertyValueMap.values().toArray(new PropertyValue[0]);
    }

    @Override
    public PropertyValue getPropertyValue(String propertyName) {
        return propertyValueMap.get(propertyName);
    }

    @Override
    public PropertyValues changesSince(PropertyValues old) {
        return null;
    }

    @Override
    public boolean contains(String propertyName) {
        return propertyValueMap.containsKey(propertyName);
    }

    @Override
    public boolean isEmpty() {
        return propertyValueMap.isEmpty();
    }
}
