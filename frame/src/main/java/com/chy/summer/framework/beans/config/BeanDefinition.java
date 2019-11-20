package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.beans.MutablePropertyValues;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.sun.istack.internal.Nullable;

import java.util.List;

public interface BeanDefinition extends AttributeAccessor {

     void setParentName(@Nullable String parentName);


     @Nullable
     String getParentName();


     void setBeanClassName(@Nullable String beanClassName);


     @Nullable
     String getBeanClassName();


     void setScope(@Nullable ScopeType scope);


     public ScopeType getScope();


     void setLazyInit(boolean lazyInit);


     boolean isLazyInit();


     void setDependsOn(@Nullable String... dependsOn);


     @Nullable
     String[] getDependsOn();


     void setAutowireCandidate(boolean autowireCandidate);


     boolean isAutowireCandidate();


     void setPrimary(boolean primary);


     boolean isPrimary();


     void setFactoryBeanName(@Nullable String factoryBeanName);


     String getFactoryBeanName();


     void setFactoryMethodName(@Nullable String factoryMethodName);


     String getFactoryMethodName();


     boolean isSingleton();


     boolean isPrototype();


     boolean isAbstract();

     @Nullable
     String getResourceDescription();

     Class<?> getBeanClass() throws IllegalStateException;

     MutablePropertyValues getPropertyValues();
}
