package com.chy.summer.framework.exception;


public abstract class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String format,Object ...param){
        this(String.format(format,param));
    }
}
