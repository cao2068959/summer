package com.chy.summer.framework.exception;

public class IllegalStateException extends  BaseRuntimeException {
    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(String format, Object... param) {
        super(format, param);
    }
}
