package com.chy.summer.framework.core.evn.resolver;


public interface ConfigurablePropertyResolver extends PropertyResolver {

    /**
     * 设置一个前缀占位符
     */
    void setPlaceholderPrefix(String placeholderPrefix);

    /**
     * 设置一个后缀占位符
     */
    void setPlaceholderSuffix(String placeholderSuffix);

    /**
     *  设置 属性值的分隔符
     */
    void setValueSeparator(String valueSeparator);


    void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);


    void setRequiredProperties(String... requiredProperties);


    void validateRequiredProperties();
}
