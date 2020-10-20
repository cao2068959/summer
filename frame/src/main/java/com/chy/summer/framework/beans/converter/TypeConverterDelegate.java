package com.chy.summer.framework.beans.converter;


import com.chy.summer.framework.util.NumberUtils;

public class TypeConverterDelegate {

    public <T> T convertIfNecessary(Object newValue, Class<T> requiredType)
            throws IllegalArgumentException {

        if (requiredType.isArray()) {
            //todo
            throw new RuntimeException("等待施工");
        }

        if (String.class == requiredType) {
            return (T) newValue.toString();
        }

        if (Number.class.isAssignableFrom(requiredType)) {
            if (newValue instanceof String) {
                return (T) NumberUtils.parseNumber((String) newValue, (Class<Number>) requiredType);
            }

            if (newValue instanceof Number) {
                return (T) NumberUtils.convertNumberToTargetClass((Number) newValue, (Class<Number>) requiredType);
            }
        }
        return (T) newValue;

    }

}
