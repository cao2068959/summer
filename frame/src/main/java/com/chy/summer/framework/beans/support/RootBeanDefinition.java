package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.context.annotation.ConfigurationClassPostProcessor;
import com.chy.summer.framework.context.annotation.constant.Autowire;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;


public class RootBeanDefinition extends AbstractBeanDefinition {


    @Getter
    private BeanDefinitionHolder decoratedDefinition;


    boolean isFactoryMethodUnique = false;

    /**
     * 前置处理或者后置处理是否开启
     */
    @Nullable
    volatile Boolean beforeInstantiationResolved;

    volatile ResolvableType targetType;

    volatile Class<?> resolvedTargetType;

    volatile ResolvableType factoryMethodReturnType;

    final Object constructorArgumentLock = new Object();

    Executable resolvedConstructorOrFactoryMethod;

    boolean constructorArgumentsResolved = false;

    Object[] resolvedConstructorArguments;

    Object[] preparedConstructorArguments;


    boolean postProcessed = false;

    public RootBeanDefinition() {
    }


    protected RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    public RootBeanDefinition(RootBeanDefinition original) {
        super(original);
        this.decoratedDefinition = original.decoratedDefinition;
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;
        this.targetType = original.targetType;
    }

    public RootBeanDefinition(Class<?> beanClass) {
        super();
        setBeanClass(beanClass);
    }

    public Class<?> getTargetType() {
        if (this.resolvedTargetType != null) {
            return this.resolvedTargetType;
        }
        ResolvableType targetType = this.targetType;
        return (targetType != null ? targetType.resolve() : null);
    }

    public void setDecoratedDefinition(@Nullable BeanDefinitionHolder decoratedDefinition) {
        this.decoratedDefinition = decoratedDefinition;
    }

    public void setUniqueFactoryMethodName(String name) {
        setFactoryMethodName(name);
        this.isFactoryMethodUnique = true;
    }

    /**
     * 判断传进来的方法 是否是 工厂方法
     *
     * @param candidate
     * @return
     */
    public boolean isFactoryMethod(Method candidate) {
        return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
    }

}
