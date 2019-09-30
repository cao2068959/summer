package com.chy.summer.framework.exception;

public class AsmException extends BaseRuntimeException {

    public AsmException(String message) {
        super(message);
    }

    public AsmException(String format, Object... param) {
        super(format, param);
    }
}
