package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DefaultListableBeanFactory extends AbstractBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(256);

    /**
     * 用来存放所有注册进来的 beanDefinition 的名字
     */
    private List<String> beanDefinitionNames = new CopyOnWriteArrayList<String>();


    /** 别名和真实 beanName 关联起来的容器
     *  key-->别名
     *  value--> 真实的beanName
     */
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);

    /**
     * 是否允许注册 bean描述的时候覆盖同名的
     */
    private boolean allowBeanDefinitionOverriding = true;


    /**
     * 手动设置的单例 的name 的容器
     */
    private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);


    private Comparator<Object> dependencyComparator;

    @Override
    public Object getBean(String name) {
        return null;
    }

    @Override
    public <T> T getBean(String name, Class<T> type) {
        return null;
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return new String[0];
    }



    /**
     * 使用对应的 beanName 注册beanDefinition
     * @param beanName
     * @param beanDefinition
     */
    private void registerBeanDefinitionHandle(String beanName, BeanDefinition beanDefinition) {
        //判断 beanFactroy  是否已经开始创建bean对象,如果已经开始了,那么现在这里还在注册 bean 就可能会有线程安全问题
        if (hasBeanCreationStarted()) {
            synchronized (this.beanDefinitionMap) {
                this.beanDefinitionMap.put(beanName, beanDefinition);
                List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
                updatedDefinitions.addAll(this.beanDefinitionNames);
                updatedDefinitions.add(beanName);
                this.beanDefinitionNames = updatedDefinitions;
            }
        }
        else {
            // 没有现成安全问题,直接创建
            this.beanDefinitionMap.put(beanName, beanDefinition);
            this.beanDefinitionNames.add(beanName);
        }
    }




    //======================BeanDefinitionRegistry 的实现方法=================================

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return beanDefinitionMap.get(beanName);
    }

    /**
     * 把 beanDefinition 注册进 容器
     * @param beanName
     * @param beanDefinition
     * @throws BeanDefinitionStoreException
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {

        //检查一波参数
        Assert.hasText(beanName, "Bean name 不能为空");
        Assert.notNull(beanDefinition, "BeanDefinition 不能为空");

        BeanDefinition oldBeanDefinition = this.beanDefinitionMap.get(beanName);
        if(oldBeanDefinition != null){
            //有同名的BeanDefinition 走覆盖逻辑
            beanDefinitionOverridingHandle(oldBeanDefinition,beanName,beanDefinition);
        }else{
            //正常的注册逻辑
            registerBeanDefinitionHandle(beanName,beanDefinition);
        }


    }

    @Override
    public void registerAlias(String name, String alias) {
        Assert.hasText(name, "'name' 不能为空");
        Assert.hasText(alias, "'alias' 不能为空");
        synchronized (this.aliasMap) {
            //如果 别名等于真实的名字,那么从别名容器里把别名移除,就算没有也要尝试一下
            if (alias.equals(name)) {
                this.aliasMap.remove(alias);
                log.debug("别名 [%s] 和 真实的beanName [%s] 一样",alias,name);
            } else {
                String registeredName = this.aliasMap.get(alias);
                //如果已经存在了相同的别名,那么只会输出一下日志,不会覆盖注册
                if (registeredName != null) {
                    if (registeredName.equals(name)) {
                        //如果 这个别名已经注册过了,而且注册对象也是相同就直接跳过了.
                        return;
                    }
                    log.info("别名 [%s] 已经被 beanName [%s] 给注册了. 新注册的 beanName [%s] 讲无权使用该别名",alias,
                            registeredName,name);
                }
                //在spring 里这里还检查了 这个别名是否 循环依赖的问题,这边用其他方法解决这个问题.
                // 别名A --> 真名B(这个真名被当做了别名又被注册了一遍指向了别名A) --> 别名A
                //这里会直接 去 beanDefinitionNames 判断一遍,真名和别名不允许重名
                if(beanDefinitionNames.contains(alias)){
                    String es = String.format("别名 [%s] 不能和beanName 重名 ",alias);
                    throw new BeanDefinitionStoreException(es);
                }
                this.aliasMap.put(alias, name);
                log.debug("别名 [%s] 已经和 beanName [%s] 关联",alias,name);
            }
        }
    }


    /**
     * 如果已经存在了对应name的 beanDefinition 存在容器中，那么这个方法登场
     */
    private void beanDefinitionOverridingHandle(BeanDefinition oldBeanDefinition, String beanName,
                                                BeanDefinition beanDefinition) {
        //容器默认不给覆盖的话，直接异常
        if(!allowBeanDefinitionOverriding){
            String es = String.format("不能注册这个 BeanDefinition [%s] : 因为对应的 beanName [%s]  已经存在 [%s] "
                    ,beanDefinition,beanName,oldBeanDefinition);
            throw new BeanDefinitionStoreException(es);
        }
        //TODO 在原版spring里面这里BeanDefinition还有优先级的概念,低优先级不能覆盖高优先级,这里先忽略后面有需要填

        //覆盖者就是本尊,给个日志直接跳过了
        if (!beanDefinition.equals(oldBeanDefinition)){
            log.info("重复注册了同一个 BeanDefinition [%s] , 对应的beanName 为 [%s]",beanDefinition,beanName);
            return;
        }
        //覆盖注册了
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }



    //======================AbstractBeanFactory 的实现方法=================================

    @Override
    public Comparator<Object> getDependencyComparator() {
        return this.dependencyComparator;
    }

    @Override
    protected Object getSingleton(String beanName, boolean b) {
        //TODO 获取单例
        return null;
    }

    public void setDependencyComparator(Comparator<Object> dependencyComparator) {
        this.dependencyComparator = dependencyComparator;
    }


    //======================ConfigurableListableBeanFactory 的实现方法=================================

    @Override
    public void ignoreDependencyType(Class<?> type) {

    }

    @Override
    public void ignoreDependencyInterface(Class<?> ifc) {

    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {

        super.registerSingleton(beanName, singletonObject);
        //如果已经开始创建bean了,说明有别的线程也在创建bean 要做对应线程安全的处理
        if (hasBeanCreationStarted()) {
            synchronized (this.beanDefinitionMap) {
                if (!this.beanDefinitionMap.containsKey(beanName)) {
                    Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames.size() + 1);
                    updatedSingletons.addAll(this.manualSingletonNames);
                    updatedSingletons.add(beanName);
                    this.manualSingletonNames = updatedSingletons;
                }
            }
        } else {
            if (!this.beanDefinitionMap.containsKey(beanName)) {
                this.manualSingletonNames.add(beanName);
            }
        }

    }
}
