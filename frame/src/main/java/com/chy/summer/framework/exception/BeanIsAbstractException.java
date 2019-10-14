package com.chy.summer.framework.exception;

public class BeanIsAbstractException extends BaseRuntimeException {

    public BeanIsAbstractException(String message) {
        super(message);
    }

    public BeanIsAbstractException(String format, Object... param) {
        super(format, param);
    }
}
