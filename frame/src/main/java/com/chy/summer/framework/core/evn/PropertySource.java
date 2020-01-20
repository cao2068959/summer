package com.chy.summer.framework.core.evn;

import lombok.Getter;

/**
 * 配置文件源数据类
 */
public abstract class PropertySource<T> {

    @Getter
    protected final String name;

    @Getter
    protected final T source;

    public PropertySource(String name, T source) {
        this.name = name;
        this.source = source;
    }


    public PropertySource(String name) {
        this(name, (T) new Object());
    }

    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    public abstract Object getProperty(String name);

}
