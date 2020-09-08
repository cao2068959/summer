package com.chy.summer.framework.beans.support.annotation;

import com.chy.summer.framework.beans.PropertyValues;
import com.chy.summer.framework.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * 用于注入的元数据对象
 */
@Slf4j
public class InjectionMetadata {

    private final Class<?> targetClass;

    private final Collection<InjectedElement> injectedElements;

    public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
        this.targetClass = targetClass;
        this.injectedElements = elements;
    }

    public static boolean needsRefresh(InjectionMetadata metadata, Class<?> clazz) {
        return (metadata == null || metadata.targetClass != clazz);
    }

    public void inject(Object target, String beanName,  PropertyValues pvs) throws Throwable {
        Collection<InjectedElement> elementsToIterate = injectedElements;
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                log.debug("执行注入, beanName: {} , 注入属性: {}",beanName,element);
                element.inject(target, beanName, pvs);
            }
        }
    }

}
