package com.chy.summer.framework.exception;

/**
 * 没有对应 BeanDefinition 异常
 */
public class NoSuchBeanDefinitionException extends BaseRuntimeException {

    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }


    public NoSuchBeanDefinitionException(String format, Object... param) {
        super(format, param);
    }
}
