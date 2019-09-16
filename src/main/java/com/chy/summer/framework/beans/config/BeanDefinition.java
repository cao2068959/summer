package com.chy.summer.framework.beans.config;

public interface BeanDefinition {

    public String getBeanClassName();

    public void setBeanClassName(String beanClassName);

    public String getScope();

    public void setScope(String scope);

    public boolean isLazyInit();

    public void setLazyInit(boolean lazyInit);
}
