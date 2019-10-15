package com.chy.summer.framework.exception;

public class BeanNotOfRequiredTypeException extends BaseRuntimeException{


    public BeanNotOfRequiredTypeException(String message) {
        super(message);
    }

    public BeanNotOfRequiredTypeException(String format, Object... param) {
        super(format, param);
    }
}
