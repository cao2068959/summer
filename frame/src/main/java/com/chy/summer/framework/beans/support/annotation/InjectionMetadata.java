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

    private volatile Set<InjectedElement> checkedElements;

    public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
        this.targetClass = targetClass;
        this.injectedElements = elements;
    }

    public static boolean needsRefresh(InjectionMetadata metadata, Class<?> clazz) {
        return (metadata == null || metadata.targetClass != clazz);
    }

    public void inject(Object target, String beanName,  PropertyValues pvs) throws Throwable {
        Collection<InjectedElement> checkedElements = this.checkedElements;
        Collection<InjectedElement> elementsToIterate =
                (checkedElements != null ? checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                log.debug("执行注入, beanName: {} , 注入属性: {}",beanName,element);
                element.inject(target, beanName, pvs);
            }
        }
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

        /**
         * 执行注入属性,这个
         * @param target
         * @param beanName
         * @param pvs
         */
        public  void inject(Object target, String beanName, PropertyValues pvs) throws Throwable{

        }
    }
}
