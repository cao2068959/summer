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
 * 需要注入的属性字段 存放在 member里面,所以也就是说每一个 属性将会有一个 AutowiredFieldElement 对象
 * bean A -> 有一个需要注入的属性 f(类型C)
 * bean B -> 有一个需要注入的属性 f1(类型C)
 * 那么 将只会有一个 AutowiredFieldElement 对象来存放 类型C , 这个对象将会作用于 bean A / bean B 
 */
public class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

    private final boolean required;

    private volatile boolean cached = false;

    private volatile Object cachedFieldValue;

    private ConfigurableListableBeanFactory beanFactory;

    public AutowiredFieldElement(Field field, ConfigurableListableBeanFactory beanFactory, boolean required) {
        super(field, null);
        this.required = required;
        this.beanFactory = beanFactory;
    }

    @Override
    public void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
        Field field = (Field) this.member;
        Object value;
        DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
        desc.setContainingClass(bean.getClass());
        Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
        Assert.state(beanFactory != null, "beanFactory 不能是Null");

        //获取属性上要注入对象的 实例
        value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames);

        //如果获取到了,就用反射把他设置进去
        if (value != null) {
            ReflectionUtils.makeAccessible(field);
            field.set(bean, value);
        }
    }
}
