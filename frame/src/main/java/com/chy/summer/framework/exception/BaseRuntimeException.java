package com.chy.summer.framework.exception;


import com.chy.summer.framework.util.BaseExceptionUtils;
import com.sun.istack.internal.Nullable;

public abstract class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String format,Object ...param){
        this(String.format(format,param));
    }

    /**
     * 检查此异常是否包含给定类型的异常:它是给定类本身的异常，还是包含给定类型的嵌套原因。
     * @param exType 要查找的异常类型
     */
    public boolean contains(@Nullable Class<?> exType) {
        if (exType == null) {
            return false;
        }
        if (exType.isInstance(this)) {
            return true;
        }
        Throwable cause = getCause();
        if (cause == this) {
            return false;
        }
        if (cause instanceof BaseRuntimeException) {
            return ((BaseRuntimeException) cause).contains(exType);
        }
        else {
            while (cause != null) {
                if (exType.isInstance(cause)) {
                    return true;
                }
                if (cause.getCause() == cause) {
                    break;
                }
                cause = cause.getCause();
            }
            return false;
        }
    }

    /**
     * 检索此异常的最具体原因，即根本原因，没有就返回异常本身
     */
    public Throwable getMostSpecificCause() {
        Throwable rootCause = getRootCause();
        return (rootCause != null ? rootCause : this);
    }

    /**
     * 检索此异常的最深层原因
     */
    @Nullable
    public Throwable getRootCause() {
        return BaseExceptionUtils.getRootCause(this);
    }
}
