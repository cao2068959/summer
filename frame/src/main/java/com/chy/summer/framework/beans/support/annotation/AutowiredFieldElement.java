package com.chy.summer.framework.beans.support.annotation;

import com.chy.summer.framework.beans.PropertyValues;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 这个类是用来执行 属性的注入的
 * 需要注入的属性字段 存放在 member属性里面,所以也就是说每一个 讲会注入的属性将会有一个 AutowiredFieldElement 对象
 */
public class AutowiredFieldElement extends InjectedElement {

    private final boolean required;

    private ConfigurableListableBeanFactory beanFactory;

    private volatile Object cachedFieldValue;

    private volatile boolean cached = false;

    public AutowiredFieldElement(Field field, ConfigurableListableBeanFactory beanFactory, boolean required) {
        super(field, null);
        Assert.state(beanFactory != null, "beanFactory 不能是Null");
        this.required = required;
        this.beanFactory = beanFactory;
    }

    @Override
    public void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
        Field field = (Field) this.member;
        Object value = getFieldValue(field, bean, beanName);
        //如果获取到了,就用反射把他设置进去
        if (value != null) {
            ReflectionUtils.makeAccessible(field);
            field.set(bean, value);
        }
    }

    private Object getFieldValue(Field field, Object bean, String beanName) {
        if (cached) {
            return cachedFieldValue;
        }

        //构造依赖注入的实体对象
        DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
        //设置一下这个 需要注入的对象所依赖的父类
        desc.setContainingClass(bean.getClass());
        Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
        //开始去 ioc容器里获取 这个 依赖属性 需要注入的对象
        Object result;
        synchronized (this) {
            if (cached) {
                return cachedFieldValue;
            }
            result = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames);
            cached = true;
            cachedFieldValue = result;
        }
        return result;
    }

}
