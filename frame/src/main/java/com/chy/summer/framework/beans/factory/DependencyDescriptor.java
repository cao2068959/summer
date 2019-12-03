package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.core.MethodParameter;
import com.chy.summer.framework.exception.BeansException;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class DependencyDescriptor extends InjectionPoint {

    //要注入字段的类型
    private  Class<?> declaringClass;
    //要注入 字段的名字
    private  String fieldName;
    //是否一定要注入 也就是 @Autowired 注解的那个属性
    @Getter
    private  boolean required;

    private  boolean eager;

    //要注入的字段所在的类
    private Class<?> containingClass;

    public DependencyDescriptor(Field field, boolean required) {
        this(field, required, true);
    }

    public DependencyDescriptor(Field field, boolean required, boolean eager) {
        super(field);
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
        this.required = required;
        this.eager = eager;
    }

    public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
        this(methodParameter, required, true);
    }


    public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
        super(methodParameter);
        this.declaringClass = methodParameter.getDeclaringClass();
        this.containingClass = methodParameter.getContainingClass();
        this.required = required;
        this.eager = eager;
    }


    public void setContainingClass(Class<?> containingClass) {
        this.containingClass = containingClass;
    }

    public Class<?> getDependencyType() {
        if (this.field != null) {
            return field.getType();
        }
        return methodParameter.getParameterType();
    }

    public boolean isEager() {
        return true;
    }

    public Annotation[] getAnnotations() {

        return null;
    }

    @Override
    public String toString() {
        return "DependencyDescriptor{" +
                "declaringClass=" + declaringClass +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }

    public String getDependencyName() {
        return this.field.getName();
    }
}
