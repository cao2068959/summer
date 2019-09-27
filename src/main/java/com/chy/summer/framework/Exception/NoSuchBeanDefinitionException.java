package com.chy.summer.framework.Exception;

/**
 * 没有对应 BeanDefinition 异常
 */
public class NoSuchBeanDefinitionException extends BaseRuntimeException {

    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }
}
