package com.chy.summer.framework.beans.support.annotation;

import com.chy.summer.framework.beans.PropertyValues;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * 执行注入元素(属性/方法) 的是个基类
 */
public abstract class InjectedElement {

    protected final Member member;

    protected final boolean isField;

    protected final PropertyDescriptor propertyDescriptor;

    protected InjectedElement(Member member, PropertyDescriptor pd) {
        this.member = member;
        this.isField = (member instanceof Field);
        this.propertyDescriptor = pd;
    }


    @Override
    public int hashCode() {
        return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + this.member;
    }

    /**
     * 执行注入属性,这个
     *
     * @param target
     * @param beanName
     * @param pvs
     */
    public void inject(Object target, String beanName, PropertyValues pvs) throws Throwable {

    }
}
