package com.chy.summer.framework.beans;


import com.chy.summer.framework.util.Assert;

public class PropertyValue {

    private final String name;

    private final Object value;

    public PropertyValue(String name, Object value) {
        Assert.notNull(name, "Name 不能为空");
        this.name = name;
        this.value = value;
    }
}
