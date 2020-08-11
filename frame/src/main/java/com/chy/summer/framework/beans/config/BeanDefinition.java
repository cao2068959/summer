package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.beans.MutablePropertyValues;
import com.chy.summer.framework.beans.factory.ConstructorArgumentValues;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.sun.istack.internal.Nullable;

import java.util.List;

/**
 *  BeanDefinition 的基础接口,描述了整个BeanDefinition的所有行为
 *
 */
public interface BeanDefinition extends AttributeAccessor {

     /**
      * 设置这个 BeanDefinition 里面真正 代理的class的全路径名称
      * @param beanClassName
      */
     void setBeanClassName(@Nullable String beanClassName);


     /**
      * 获取 真正代理class的全路径名称
      * @return
      */
     @Nullable
     String getBeanClassName();

     /**
      * 设置 生成对象实例的作用域
      * @param scope
      */
     void setScope(@Nullable ScopeType scope);

     /**
      * 获取对应的作用域
      * @return
      */
     ScopeType getScope();

     /**
      * 设置是否懒加载
      * @param lazyInit
      */
     void setLazyInit(boolean lazyInit);

     /**
      * 是否懒加载
      * @return
      */
     boolean isLazyInit();

     /**
      * 设置依赖
      * @param dependsOn
      */
     void setDependsOn(@Nullable String... dependsOn);

     /**
      * 获取对应的依赖
      * @return
      */
     @Nullable
     String[] getDependsOn();


     void setAutowireCandidate(boolean autowireCandidate);


     boolean isAutowireCandidate();


     void setPrimary(boolean primary);


     boolean isPrimary();

     /**
      * 设置 FactoryBeanName
      * @param factoryBeanName
      */
     void setFactoryBeanName(@Nullable String factoryBeanName);

     /**
      * 获取 FactoryBeanName
      * @return
      */
     String getFactoryBeanName();

     /**
      * 设置 工厂方法 方法名
      * @return
      */
     void setFactoryMethodName(@Nullable String factoryMethodName);

     /**
      * 获取 factory 方法名
      * @return
      */
     String getFactoryMethodName();

     /**
      * 是否是单例
      * @return
      */
     boolean isSingleton();


     boolean isPrototype();

     /**
      * 是否是抽象类
      * @return
      */
     boolean isAbstract();

     @Nullable
     String getResourceDescription();

     Class<?> getBeanClass() throws IllegalStateException;

     MutablePropertyValues getPropertyValues();

     ConstructorArgumentValues getConstructorArgumentValues();
}
