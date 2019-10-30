package com.chy.summer.framework.beans.support.annotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 用于注入的元数据对象
 */
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


    public abstract static class InjectedElement {

        protected final Member member;

        protected final boolean isField;

        protected final PropertyDescriptor propertyDescriptor;

        protected InjectedElement(Member member,  PropertyDescriptor pd) {
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
    }
}
