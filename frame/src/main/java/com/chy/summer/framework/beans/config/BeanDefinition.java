package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.context.annotation.constant.ScopeType;

public interface BeanDefinition {

     String getBeanClassName();

     void setBeanClassName(String beanClassName);

     ScopeType getScope();

     void setScope(ScopeType scope);

     boolean isLazyInit();

     void setLazyInit(boolean lazyInit);
}
