package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanDefinition;

public abstract class AbstractBeanDefinition implements BeanDefinition {

    private String beanClassName;

    private String scope;

    private boolean lazyInit;

    @Override
    public String getBeanClassName() {
        return null;
    }

    @Override
    public void setBeanClassName(String beanClassName) {

    }

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public void setScope(String scope) {

    }

    @Override
    public boolean isLazyInit() {
        return false;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {

    }
}
