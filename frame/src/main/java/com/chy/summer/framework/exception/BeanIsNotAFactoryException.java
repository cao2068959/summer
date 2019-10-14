package com.chy.summer.framework.exception;


public class BeanIsNotAFactoryException extends BaseRuntimeException {
    public BeanIsNotAFactoryException(String message) {
        super(message);
    }

    public BeanIsNotAFactoryException(String beanName, Class<?> aClass) {
        super("beanName: [%s]，类: [%s] 不是FactroyBean", beanName,aClass);
    }

    public BeanIsNotAFactoryException(String format, Object... param) {
        super(format, param);
    }
}
