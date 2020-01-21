package com.chy.summer.framework.core.evn.resolver;


public interface PropertyResolver {

    /**
     * 是否包含某个key的属性
     */
    boolean containsProperty(String key);

    /**
     * 获取某个key 的属性
     */
    String getProperty(String key);

    /**
     * 获取某个key 的属性，如果没有就给默认值
     */
    String getProperty(String key, String defaultValue);

    /**
     * 获取某个key 的属性，并且返回指定的泛型类型
     */
    <T> T getProperty(String key, Class<T> targetType);

    /**
     * 获取某个key 的属性，并且返回指定的泛型类型，并且给与了对应的默认值
     */
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);


    /**
     * 同上面的 getProperty，区别在于如果没有对应的值则会抛出异常
     */
    String getRequiredProperty(String key) throws IllegalStateException;


    /**
     * 同上面的 getProperty，区别在于如果没有对应的值则会抛出异常
     */
    <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

    /**
     * 解析占位符${...}，站位符的值来自 getProperty
     * @param text
     * @return
     */
    String resolvePlaceholders(String text);

    String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
