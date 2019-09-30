package com.chy.summer.framework.exception;


public class BeansException extends BaseRuntimeException{
    public BeansException(String message) {
        super(message);
    }

    public BeansException(String format, Object... param) {
        super(format, param);
    }
}
