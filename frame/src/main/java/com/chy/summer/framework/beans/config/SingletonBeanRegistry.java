package com.chy.summer.framework.beans.config;

public interface SingletonBeanRegistry {

    void registerSingleton(String beanName, Object singletonObject);


    Object getSingleton(String beanName);


    boolean containsSingleton(String beanName);


    String[] getSingletonNames();


    int getSingletonCount();


    Object getSingletonMutex();

}
