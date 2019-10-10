package com.chy.summer.framework.exception;

public class CannotLoadBeanClassException extends BaseRuntimeException {
    public CannotLoadBeanClassException(String message) {
        super(message);
    }

    public CannotLoadBeanClassException(String format, Object... param) {
        super(format, param);
    }
}
