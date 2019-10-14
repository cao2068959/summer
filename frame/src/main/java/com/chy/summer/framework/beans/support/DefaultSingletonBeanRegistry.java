package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.factory.ObjectFactory;
import com.chy.summer.framework.exception.IllegalStateException;
import com.chy.summer.framework.util.Assert;

import java.util.*;
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
    /** 正在创建的单例对象*/
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /** 已经用FactroyBean 生成好的对象的缓存*/
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);


    /**
     * 获取单例对象
     * @param beanName
     * @param allowEarlyReference
     * @return
     */
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        Object singletonObject = this.singletonObjects.get(beanName);
        //缓存里没有，并且并不是正在创建中，那么就继续
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                //在单例对象的容器里没有找到，那么去半成品的容器里 瞅一瞅有没有
                singletonObject = this.earlySingletonObjects.get(beanName);
                //半成品也没有，那么如果传入了 allowEarlyReference 允许现场造一个那么就继续
                if (singletonObject == null && allowEarlyReference) {
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
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

    /**
     *  从 FactoryBean 里去获取对象然后放入容器
     * @param factory
     * @param beanName
     * @param shouldPostProcess
     * @return
     */
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess){
        //判断是不是单例
        if (factory.isSingleton() && containsSingleton(beanName)){

        }else{

        }
    }


    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

    protected Object getCachedObjectForFactoryBean(String beanName) {
        return this.factoryBeanObjectCache.get(beanName);
    }

}
