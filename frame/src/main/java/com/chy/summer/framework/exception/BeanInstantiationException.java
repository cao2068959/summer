package com.chy.summer.framework.exception;

public class BeanInstantiationException extends BaseRuntimeException{
    public BeanInstantiationException(String message) {
        super(message);
    }

    public BeanInstantiationException(String format, Object... param) {
        super(format, param);
    }
}
