package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.*;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.factory.AutowireCandidateResolver;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static com.chy.summer.framework.util.BeanFactoryUtils.transformedBeanName;

@Slf4j
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

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

    /**
     * 根据类型查找beanName的缓存
     */
    private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);
    /**
     * 根据类型查找beanName的缓存,这里是单例对象的
     */
    private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);



    private volatile boolean configurationFrozen = false;

    private AutowireCandidateResolver autowireCandidateResolver = null;




    private Comparator<Object> dependencyComparator;



    /**
     * 根据类型获取 bean的name
     * @param type
     * @param includeNonSingletons
     * @param allowEagerInit
     * @return
     */
    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

        //根据是不是单列选择不同的缓存对象
        Map<Class<?>, String[]> cache = (includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
        String[] resolvedBeanNames = cache.get(type);
        //有缓存直接返回
        if (resolvedBeanNames != null) {
            return resolvedBeanNames;
        }
        //真正去判断ioc容器里 有没对应类型的bean
        resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forClass(type), includeNonSingletons, true);
        //写入缓存
        cache.put(type, resolvedBeanNames);
        return resolvedBeanNames;
    }

    /**
     * 根据类型获取所有满足条件的bean的name
     * @param type
     * @param includeNonSingletons
     * @param allowEagerInit
     * @return
     */
    private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        List<String> result = new ArrayList<>();

        for (String beanName : this.beanDefinitionNames) {
            //如果是别名,直接跳过
            if (isAlias(beanName)) {
                continue;
            }

            try {
                //通过beanName名字拿到 RootBeanDefinition
                RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                if (!mbd.isAbstract() && (allowEagerInit || !mbd.isLazyInit())) {
                    //判断是不是 FactoryBean
                    boolean isFactoryBean = isFactoryBean(beanName);
                    BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
                    boolean matchFound =
                            (allowEagerInit || !isFactoryBean || (dbd != null && !mbd.isLazyInit()) || containsSingleton(beanName))
                                    &&
                            (includeNonSingletons || (dbd != null ? mbd.isSingleton() : false))
                                    &&
                             //这里是直接用beanName获取单例对象 然后用 instanceof 去对比类型
                            isTypeMatch(beanName, type.resolve());
                    //如果比较失败,并且还是一个 FactoryBean 那么就加上& 再比较一次
                    if (!matchFound && isFactoryBean) {
                        beanName = FACTORY_BEAN_PREFIX + beanName;
                        matchFound = (includeNonSingletons || mbd.isSingleton()) && isTypeMatch(beanName, type.resolve());
                    }
                    if (matchFound) {
                        result.add(beanName);
                    }
                }
            }
            catch (Exception ex) {
                if (allowEagerInit) {
                    throw ex;
                }
                log.info("根据类型 [{}] 获取所有满足条件的beanName 失败 原因: [{}]",type,ex.getMessage());
            }

        }

        // 同样手动注册进去的单列也要检查
        for (String beanName : this.manualSingletonNames) {
            try {
                // In case of FactoryBean, match object created by FactoryBean.
                if (isFactoryBean(beanName)) {
                    if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type.resolve())) {
                        result.add(beanName);
                        continue;
                    }
                    // In case of FactoryBean, try to match FactoryBean itself next.
                    beanName = FACTORY_BEAN_PREFIX + beanName;
                }
                if (isTypeMatch(beanName, type.resolve())) {
                    result.add(beanName);
                }
            }
            catch (NoSuchBeanDefinitionException ex) {
                log.info("检查手动注册的单列失败 对应 名称为 [{}] 失败原因为: {}" ,beanName,ex.getMessage());
            }
        }

        return StringUtils.toStringArray(result);
    }

    /**
     * 检查对应的beanName 是不是单例
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                return (BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton());
            }
            else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

        if (mbd.isSingleton()) {
            if (isFactoryBean(beanName)) {
                if (BeanFactoryUtils.isFactoryDereference(name)) {
                    return true;
                }
                FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
                return factoryBean.isSingleton();
            }
            else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        }
        else {
            return false;
        }
    }


    /**
     * 判断是否是别名
     * @param name
     * @return
     */
    public boolean isAlias(String name) {
        return this.aliasMap.containsKey(name);
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
                log.debug("别名 [{}] 和 真实的beanName [{}] 一样",alias,name);
            } else {
                String registeredName = this.aliasMap.get(alias);
                //如果已经存在了相同的别名,那么只会输出一下日志,不会覆盖注册
                if (registeredName != null) {
                    if (registeredName.equals(name)) {
                        //如果 这个别名已经注册过了,而且注册对象也是相同就直接跳过了.
                        return;
                    }
                    log.info("别名 [{}] 已经被 beanName [{}] 给注册了. 新注册的 beanName [{}] 讲无权使用该别名",alias,
                            registeredName,name);
                }
                //在spring 里这里还检查了 这个别名是否 循环依赖的问题,这边用其他方法解决这个问题.
                // 别名A --> 真名B(这个真名被当做了别名又被注册了一遍指向了别名A) --> 别名A
                //这里会直接 去 beanDefinitionNames 判断一遍,真名和别名不允许重名
                if(beanDefinitionNames.contains(alias)){
                    String es = String.format("别名 [{}] 不能和beanName 重名 ",alias);
                    throw new BeanDefinitionStoreException(es);
                }
                this.aliasMap.put(alias, name);
                log.debug("别名 [{}] 已经和 beanName [{}] 关联",alias,name);
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


    public Object getAutowireCandidateResolver() {
        return this.autowireCandidateResolver;
    }

    public void setAutowireCandidateResolver(final AutowireCandidateResolver autowireCandidateResolver) {
        Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
        if (autowireCandidateResolver instanceof BeanFactoryAware) {
            ((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
        }
        this.autowireCandidateResolver = autowireCandidateResolver;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return  StringUtils.toStringArray(this.beanDefinitionNames);
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

    @Override
    public void freezeConfiguration() {
        this.configurationFrozen = true;
    }

    @Override
    public void preInstantiateSingletons() {
        log.debug("==============预先初始化单列对象 开始========== ;");

        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

        //迭代所有的 beanDefinitionNames
        for (String beanName : beanNames) {
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            //如果不是抽象类,不是懒加载,并且是单例,才走下面
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                if (isFactoryBean(beanName)) {
                   //TODO FactoryBean 的初始化流程,这里只会先初始化 SmartFactoryBean 接口的
                }
                else {
                    //这里就直接初始化 单例对象了.
                    getBean(beanName);
                }
            }
        }

        //TODO 在 bean 生成单列对象后 , 如果实现了 SmartInitializingSingleton 接口,还会调用 这个接口去做一些后置处理的事情
    }


    /**
     * 获取合并后的  BeanDefinition
     * 这里有一层缓存
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {

        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null) {
            return mbd;
        }
        return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
    }

    private RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        return  getMergedBeanDefinition(beanName,beanDefinition,null);
    }


    /**
     * 用对应的 name 来判断一下 是不是  BeanFactory
     * 1 . 去直接拿他实例化后的对象 instanceof FactoryBean 来判断
     * 2 . 如果是非单列对象,这里要去拿 BeanDefinition 去判断
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
        String beanName = BeanFactoryUtils.transformedBeanName(name);

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            return (beanInstance instanceof FactoryBean);
        }
        //TODO 如果不是单列对象 要去判断 BD,这里先只考虑单例
        return false;
    }

    @Override
    public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
        String beanName = transformedBeanName(name);

        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
            return ((ConfigurableListableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName);
        }
        return getMergedLocalBeanDefinition(beanName);
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    @Override
    public boolean isCurrentlyInCreation(String beanName) {
        return super.isSingletonCurrentlyInCreation(beanName);
    }

    @Override
    public Object getSingletonMutex() {
        return getSingletonObjects();
    }
}
