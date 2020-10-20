package com.chy.summer.framework.beans.converter;

import com.chy.summer.framework.core.MethodParameter;
import com.chy.summer.framework.util.Assert;
import lombok.Setter;

import java.lang.reflect.Field;

public abstract class TypeConverterSupport implements TypeConverter {

    @Setter
    TypeConverterDelegate typeConverterDelegate;


    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType) {
        return doConvert(value, requiredType, null, null);
    }

    private <T> T doConvert(Object value, Class<T> requiredType,
                            MethodParameter methodParam, Field field) {
        Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
        return this.typeConverterDelegate.convertIfNecessary(value, requiredType);
    }


}
