package com.chy.summer.framework.beans.factory;

import java.lang.reflect.Field;

public class DependencyDescriptor extends InjectionPoint {

    //要注入字段的类型
    private final Class<?> declaringClass;
    //要注入 字段的名字
    private final String fieldName;
    //是否一定要注入 也就是 @Autowired 注解的那个属性
    private final boolean required;

    private final boolean eager;

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

    public void setContainingClass(Class<?> containingClass) {
        this.containingClass = containingClass;
    }

    public Class<?> getDependencyType() {
        return this.field.getType();
    }
}
