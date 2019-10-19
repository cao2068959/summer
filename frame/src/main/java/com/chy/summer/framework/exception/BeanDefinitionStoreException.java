package com.chy.summer.framework.exception;


public class BeanDefinitionStoreException extends BaseRuntimeException{

    public BeanDefinitionStoreException(String message) {
        super(message);
    }

    public BeanDefinitionStoreException(String format, Object... param) {
        super(format, param);
    }
}
