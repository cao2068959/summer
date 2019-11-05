package com.chy.summer.framework.beans;

import com.chy.summer.framework.exception.BeanInstantiationException;
import com.chy.summer.framework.util.Assert;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanUtils {

    /**
     *  propertyDescriptor 的缓存 key:class的全路径,value:这个类里面所有的PropertyDescriptor
     */
    public static Map<String,PropertyDescriptor[]> propertyDescriptorCache = new ConcurrentHashMap<>();

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


    /**
     * 从class中 获取对应 方法的 PropertyDescriptor
     * @param method
     * @param clazz
     * @return
     */
    public static PropertyDescriptor findPropertyForMethod(Method method, Class<?> clazz) {
        Assert.notNull(method, "Method 不能为null ");
        //拿到对应class里的所有PropertyDescriptor
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        //循环看入参进来的方法 是 setter或者getter方法吗?是的话就返回 PropertyDescriptor
        for (PropertyDescriptor pd : pds) {
            if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
                return pd;
            }
        }
        return null;
    }

    /**
     * 获取一个类的 PropertyDescriptor 这里有缓存
     * @param beanClass
     * @return
     */
    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass) {

        PropertyDescriptor[] result = propertyDescriptorCache.get(beanClass.getName());
        if(result != null){
            return result;
        }

        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        result = beanInfo.getPropertyDescriptors();
        propertyDescriptorCache.put(beanClass.getName(),result);

        return result;
    }
}
