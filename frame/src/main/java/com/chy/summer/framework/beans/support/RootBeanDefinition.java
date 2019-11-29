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


    private BeanDefinitionHolder decoratedDefinition;

    private AnnotatedElement qualifiedElement;

    boolean allowCaching = true;

    boolean isFactoryMethodUnique = false;


    /**
     * 前置处理或者后置处理是否开启
     */
    @Nullable
    volatile Boolean beforeInstantiationResolved = true;

    @Getter
    @Setter
    private String factoryBeanName;

    @Getter
    @Setter
    private String factoryMethodName;

    volatile ResolvableType targetType;

    volatile Class<?> resolvedTargetType;

    volatile ResolvableType factoryMethodReturnType;

    final Object constructorArgumentLock = new Object();

    Executable resolvedConstructorOrFactoryMethod;

    boolean constructorArgumentsResolved = false;

    Object[] resolvedConstructorArguments;

    Object[] preparedConstructorArguments;

    final Object postProcessingLock = new Object();

    boolean postProcessed = false;

    public RootBeanDefinition() {
    }


    protected RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    public RootBeanDefinition(RootBeanDefinition original) {
        super(original);
        this.decoratedDefinition = original.decoratedDefinition;
        this.qualifiedElement = original.qualifiedElement;
        this.allowCaching = original.allowCaching;
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;
        this.targetType = original.targetType;
    }

    public RootBeanDefinition(Class<?> beanClass) {
        super();
        setBeanClass(beanClass);
    }

    @Override
    public void setParentName(String parentName) {

    }

    @Override
    public String getParentName() {
        return null;
    }

    @Override
    public void setDependsOn(String... dependsOn) {

    }

    @Override
    public String[] getDependsOn() {
        return new String[0];
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {

    }

    @Override
    public boolean isAutowireCandidate() {
        return false;
    }

    @Override
    public void setPrimary(boolean primary) {

    }

    @Override
    public boolean isPrimary() {
        return false;
    }

    @Override
    public boolean isPrototype() {
        return false;
    }



    @Override
    public String getResourceDescription() {
        return null;
    }




    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }

    public BeanDefinitionHolder getDecoratedDefinition() {
        return this.decoratedDefinition;
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
     * @param candidate
     * @return
     */
    public boolean isFactoryMethod(Method candidate) {
        return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
    }

}
