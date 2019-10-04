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
            ctor.newInstance(args);
        }
        catch (InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
            throw new BeanInstantiationException("创建bean异常: [%s]",ex.getMessage());
        }
        return null;
    }
}
