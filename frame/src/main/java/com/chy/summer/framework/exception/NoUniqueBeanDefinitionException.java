package com.chy.summer.framework.exception;

public class NoUniqueBeanDefinitionException extends BaseRuntimeException {

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }


    public NoUniqueBeanDefinitionException(String format, Object... param) {
        super(format, param);
    }
}
