package com.chy.summer.framework.beans;

public interface FactoryBean<T> {

    Class<?> getObjectType();

    boolean isSingleton();

    Object getObject();
}
