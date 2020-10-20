package com.chy.summer.framework.beans.converter;

public class SimpleTypeConverter extends TypeConverterSupport {

    public SimpleTypeConverter() {
        setTypeConverterDelegate(new TypeConverterDelegate());
    }
}
