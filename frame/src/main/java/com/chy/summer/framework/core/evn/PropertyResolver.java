package com.chy.summer.framework.core.evn;

public interface PropertyResolver {

    /**
     * 查看某个Key 有没存在
     * @param key
     * @return
     */
    boolean containsProperty(String key);


    /**
     * 通过key 获取 属性
     * @param key
     * @return
     */
    String getProperty(String key);


    /**
     * 同上多了个默认值
     * @param key
     * @param defaultValue
     * @return
     */
    String getProperty(String key, String defaultValue);

    /**
     * 同上
     * @param key
     * @param targetType
     * @param <T>
     * @return
     */

    <T> T getProperty(String key, Class<T> targetType);

    /**
     * 同上
     * @param key
     * @param targetType
     * @param <T>
     * @return
     */
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);


    String getRequiredProperty(String key) throws IllegalStateException;


    <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;


    String resolvePlaceholders(String text);


    String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
