package com.chy.summer.framework.core.evn;


import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 多个配置文件的接口
 */
public interface PropertySources extends Iterable<PropertySource<?>> {


    default Stream<PropertySource<?>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }


    boolean contains(String name);


    PropertySource<?> get(String name);
}
