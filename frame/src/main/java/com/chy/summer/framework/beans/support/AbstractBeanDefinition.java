package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.chy.summer.framework.core.io.support.Resource;

import java.util.function.Supplier;

public abstract class AbstractBeanDefinition implements BeanDefinition {

    private String beanClassName;
    private ScopeType scope;
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

    protected AbstractBeanDefinition(BeanDefinition original) {
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setScope(original.getScope());
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

    public AbstractBeanDefinition() {
    }

    public void setInstanceSupplier( Supplier<?> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }
}
