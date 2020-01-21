package com.chy.summer.framework.core.evn.resolver;


import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.PropertyPlaceholderHelper;
import com.chy.summer.framework.util.SystemPropertyUtils;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

    /**
     * 占位符的后缀，默认是 }
     */
    private String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    /**
     * 占位符的前缀，默认是 ${
     */
    private String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

    /**
     * 分隔符，默认是 ：
     */
    @Setter
    private String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;

    @Setter
    private boolean ignoreUnresolvableNestedPlaceholders = false;

    private final Set<String> requiredProperties = new LinkedHashSet<>();

    private PropertyPlaceholderHelper nonStrictHelper;


    //==========================================================================================
    //                    ConfigurablePropertyResolver 接口 的实现
    //==========================================================================================

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * 设置一个前缀占位符
     *
     * @param placeholderPrefix
     */
    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
        this.placeholderPrefix = placeholderPrefix;
    }


    @Override
    public void setRequiredProperties(String... requiredProperties) {
        Collections.addAll(this.requiredProperties, requiredProperties);
    }

    /**
     * 校验 required的key是否有效，其实也就是 调用 getProperty 去判断能不能拿到值
     */
    @Override
    public void validateRequiredProperties() {
        List<String> invalid = new ArrayList<>();

        for (String key : this.requiredProperties) {
            if (this.getProperty(key) == null) {
                invalid.add(key);
            }
        }
        if (!invalid.isEmpty()) {
            String collect = invalid.stream().collect(Collectors.joining(","));
            throw new RuntimeException("无效的 requiredProperties: [" + collect + "]");
        }
    }

    //==========================================================================================
    //                    PropertyResolver 接口 的实现
    //==========================================================================================

    /**
     * 是否包含某个key的属性
     *
     * @param key
     */
    @Override
    public boolean containsProperty(String key) {
        return (getProperty(key) != null);
    }

    /**
     * 获取某个key 的属性
     *
     * @param key
     */
    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    /**
     * 获取某个key 的属性，如果没有就给默认值
     *
     * @param key
     * @param defaultValue
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null ? value : defaultValue);
    }

    /**
     * 获取某个key 的属性，并且返回指定的泛型类型，并且给与了对应的默认值
     *
     * @param key
     * @param targetType
     * @param defaultValue
     */
    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return (value != null ? value : defaultValue);
    }

    /**
     * 同上面的 getProperty，区别在于如果没有对应的值则会抛出异常
     *
     * @param key
     */
    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Required key '" + key + "' not found");
        }
        return value;
    }

    /**
     * 同上面的 getProperty，区别在于如果没有对应的值则会抛出异常
     *
     * @param key
     * @param targetType
     */
    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        T value = getProperty(key,targetType);
        if (value == null) {
            throw new IllegalStateException("Required key '" + key + "' not found");
        }
        return value;
    }


    /**
     * 解析占位符${...}，站位符的值来自 getProperty
     *
     * @param text
     * @return
     */
    @Override
    public String resolvePlaceholders(String text) {
        if (this.nonStrictHelper == null) {
            this.nonStrictHelper = createPlaceholderHelper(true);
        }
        return doResolvePlaceholders(text, this.nonStrictHelper);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return null;
    }


    //==========================================================================================
    //                    这个类里面本身的方法
    //==========================================================================================


    private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
        return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix,
                this.valueSeparator, ignoreUnresolvablePlaceholders);
    }

    private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
        return helper.replacePlaceholders(text, this::getPropertyAsRawString);
    }

}
