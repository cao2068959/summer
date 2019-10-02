package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.factory.ObjectFactory;
import com.chy.summer.framework.exception.IllegalStateException;
import com.chy.summer.framework.util.Assert;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例对象的处理都在这里面
 */
public class DefaultSingletonBeanRegistry {

    /**
     * 单例对象的容器
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /** 缓存单例对象的工厂:  name --> ObjectFactory */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    /** 缓存半成品的单例对象 */
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    /** 把单例对象的名字都给放进去 */
    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

    protected boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    protected  Object getSingleton(String beanName, boolean b){
        return null;
    }


    /**
     * 注册一个单例对象到Ioc 容器里
     * @param beanName
     * @param singletonObject
     */
    void registerSingleton(String beanName, Object singletonObject){
        Assert.notNull(beanName, "注册单例对象的时候 beanName 不能为空");
        Assert.notNull(singletonObject, "单例对象不能为空");
        synchronized (this.singletonObjects) {
            Object oldObject = this.singletonObjects.get(beanName);
            if (oldObject != null) {
                throw new IllegalStateException("不能够注册单列对象 [%s] 因为对应的beanName [%s] 已经被对象 [%s] 给占用",
                        singletonObject, beanName, oldObject);
            }
            addSingleton(beanName, singletonObject);
        }
    }

    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

}
