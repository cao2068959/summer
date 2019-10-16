package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.ClassUtils;

import java.util.function.Supplier;

public abstract class AbstractBeanDefinition implements BeanDefinition {

    private String beanClassName;
    private ScopeType scope = ScopeType.SINGLETON;
    private boolean lazyInit = false;
    private Resource resource;
    private boolean abstractFlag = false;

    private volatile Object beanClass;

    private Supplier<?> instanceSupplier;



    @Override
    public String getBeanClassName() {
        return beanClassName;
    }

    @Override
    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    @Override
    public ScopeType getScope() {
        return scope;
    }

    @Override
    public void setScope(ScopeType scope) {
        this.scope = scope;
    }

    @Override
    public boolean isLazyInit() {
        return lazyInit;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    @Override
    public boolean isSingleton() {
        return ScopeType.SINGLETON == this.scope;
    }

    public Resource getResource() {
        return resource;
    }

    public boolean isAbstractFlag() {
        return abstractFlag;
    }

    public void setAbstractFlag(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    protected AbstractBeanDefinition(BeanDefinition original) {
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setScope(original.getScope());
        setBeanClass(original.getBeanClass());
        //setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());
        //setSource(original.getSource());
        //copyAttributesFrom(original);

        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            setPrimary(originalAbd.isPrimary());
            setResource(originalAbd.getResource());
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append("]");
        sb.append("; scope=").append(this.scope);
        sb.append("; lazyInit=").append(this.lazyInit);
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }

    public void setBeanClass( Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    public AbstractBeanDefinition() {
    }

    public Class<?> resolveBeanClass( ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    public boolean hasMethodOverrides() {
        return false;
    }

    public void setInstanceSupplier( Supplier<?> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }


}
