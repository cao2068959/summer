package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.chy.summer.framework.core.io.support.Resource;

public abstract class AbstractBeanDefinition implements BeanDefinition {

    private String beanClassName;

    private ScopeType scope;

    private boolean lazyInit = false;
    private Resource resource;

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


}