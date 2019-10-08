package com.chy.summer.framework.beans;

import com.chy.summer.framework.exception.BeanInstantiationException;
import com.chy.summer.framework.util.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class BeanUtils {

    /**
     * 通过构造器实例化对象
     * @return
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
           return ctor.newInstance(args);
        }
        catch (InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
            throw new BeanInstantiationException("创建bean异常: [%s]",ex.getMessage());
        }
    }

    public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {

        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            return instantiateClass(ctor);
        }
        catch (Exception | LinkageError ex) {
            throw new BeanInstantiationException("class: [%s],实例化异常: [%s]",ex.getMessage());
        }

    }
}
