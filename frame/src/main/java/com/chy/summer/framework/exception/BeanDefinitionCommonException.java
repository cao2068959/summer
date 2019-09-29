package com.chy.summer.framework.exception;


public class BeanDefinitionCommonException extends BaseRuntimeException {
    public BeanDefinitionCommonException(String message) {
        super(message);
    }

    public BeanDefinitionCommonException(String format, Object... param) {
        super(format, param);
    }

}
