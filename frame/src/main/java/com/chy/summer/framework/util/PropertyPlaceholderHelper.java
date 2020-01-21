package com.chy.summer.framework.util;

import java.util.Set;

/**
 * 用了解析占位符的工具类
 */
public class PropertyPlaceholderHelper {

    private final String placeholderPrefix;
    private final boolean ignoreUnresolvablePlaceholders;
    private final String placeholderSuffix;
    private final String valueSeparator;

    public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
                                     String valueSeparator, boolean ignoreUnresolvablePlaceholders) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    /**
     * 用于解析 占位符的方法
     */
    protected String parseStringValue(
            String value, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders){

        int startIndex = value.indexOf(this.placeholderPrefix);
        //如果传入的 value 不满足占位符的 前缀，那么说明没有占位符，直接返回
        if (startIndex == -1) {
            return value;
        }




    }




    public interface PlaceholderResolver {

        String resolvePlaceholder(String placeholderName);
    }

}
