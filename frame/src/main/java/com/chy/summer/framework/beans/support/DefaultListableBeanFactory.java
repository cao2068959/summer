package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.config.*;
import com.chy.summer.framework.beans.factory.AutowireCandidateResolver;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.exception.*;
import com.chy.summer.framework.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.IllegalStateException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.chy.summer.framework.util.BeanFactoryUtils.transformedBeanName;

@Slf4j
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(256);

    /**
     * 用来存放所有注册进来的 beanDefinition 的名字
     */
    private List<String> beanDefinitionNames = new CopyOnWriteArrayList<String>();


    /**
     * 别名和真实 beanName 关联起来的容器
     * key-->别名
     * value--> 真实的beanName
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


    /**
     * 正向的依赖  A 依赖 <B,C,D>
     */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

    /**
     * 这里面存  B 被什么bean给依赖了 <A,C>
     */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

    private volatile boolean configurationFrozen = false;


    /**
     * 自动注入解析器,可以通过自定义添加这个解析器来自定义 注入的行为, 比如 @value注解 里面 ${} 表达式的解析
     */
    @Getter
    private AutowireCandidateResolver autowireCandidateResolver = null;


    private Comparator<Object> dependencyComparator;


    /**
     * `
     * 把bean之间的依赖关系存入
     *
     * @param beanName
     * @param dependentBeanName
     */
    @Override
    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = canonicalName(beanName);
        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName,
                    k -> new LinkedHashSet<>(8));
            //添加失败,说明已经有对应的值了 后面就不执行了
            if (!dependentBeans.add(dependentBeanName)) {
                return;
            }
        }

        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName,
                    k -> new LinkedHashSet<>(8));
            dependenciesForBean.add(canonicalName);
        }
    }

    private String canonicalName(String encoding) {
        if (encoding == null) {
            return null;
        }
        try {
            return Charset.forName(encoding).name();
        } catch (IllegalCharsetNameException | UnsupportedCharsetException icne) {
            return encoding;
        }
    }


    /**
     * 根据 class的类型,获取可能和他匹配的所有 beanName
     *
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
        //真正去判断ioc容器里 有没对应类型的bean,有的话把他生成出来
        resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forClass(type), includeNonSingletons, allowEagerInit);
        //写入缓存
        cache.put(type, resolvedBeanNames);
        return resolvedBeanNames;
    }

    /**
     * 根据类型获取所有满足条件的bean的name
     *
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

                    boolean matchFound = isMatchBean(beanName, allowEagerInit, includeNonSingletons, type, mbd);
                    if (matchFound) {
                        result.add(beanName);
                    }
                }
            } catch (Exception ex) {
                if (allowEagerInit) {
                    throw ex;
                }
                log.info("根据类型 [{}] 获取所有满足条件的beanName 失败 原因: [{}]", type, ex.getMessage());
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
            } catch (NoSuchBeanDefinitionException ex) {
                log.info("检查手动注册的单列失败 对应 名称为 [{}] 失败原因为: {}", beanName, ex.getMessage());
            }
        }

        return StringUtils.toStringArray(result);
    }

    private boolean isMatchBean(String beanName,
                                boolean allowEagerInit,
                                boolean includeNonSingletons,
                                ResolvableType type,
                                RootBeanDefinition mbd) {
        //判断是否是 工厂bean
        boolean isFactoryBean = isFactoryBean(beanName);
        BeanDefinitionHolder beanDefinitionHolder = mbd.getDecoratedDefinition();
        //如果半成品的 bean不参与匹配 ,那么将进入以下if去检查 bean到底是不是半成品
        if (!allowEagerInit) {
           /* if (isFactoryBean || (beanDefinitionHolder != null && !mbd.isLazyInit()) || containsSingleton(beanName)) {
                return false;
            }*/
        }


        //如果非单例的 bean不允许匹配  就去检查一下 这个bean到底是不是单例
        if (!includeNonSingletons) {
            //如果他没设置了 beanDefinitionHolder 那么使用 isSingleton() 去判断单例,否则使用  mbd.isSingleton()
            if (beanDefinitionHolder == null) {
                if (!isSingleton(beanName)) {
                    return false;
                }
            } else {
                if (!mbd.isSingleton()) {
                    return false;
                }
            }
        }

        //最后去匹配 beanName 对应的类型是否 匹配
        boolean typeMatch = isTypeMatch(beanName, type.resolve());
        if (typeMatch) {
            return typeMatch;
        }
        //如果没有匹配上 但是确是是个  FactoryBean 那么就加上& 再比较一次
        if (isFactoryBean && (includeNonSingletons || mbd.isSingleton())) {
            beanName = FACTORY_BEAN_PREFIX + beanName;
            return isTypeMatch(beanName, type.resolve());
        }

        return typeMatch;
    }


    /**
     * 检查对应的beanName 是不是单例
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                return (BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton());
            } else {
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
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        } else {
            return false;
        }
    }


    /**
     * 判断是否是别名
     *
     * @param name
     * @return
     */
    public boolean isAlias(String name) {
        return this.aliasMap.containsKey(name);
    }

    /**
     * 使用对应的 beanName 注册beanDefinition
     *
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
        } else {
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
     *
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
        if (oldBeanDefinition != null) {
            //有同名的BeanDefinition 走覆盖逻辑
            beanDefinitionOverridingHandle(oldBeanDefinition, beanName, beanDefinition);
        } else {
            //正常的注册逻辑
            registerBeanDefinitionHandle(beanName, beanDefinition);
        }
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        Assert.hasText(beanName, "'beanName' 不能为空");
        BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
        if (bd == null) {
            log.error("没有发现 bean name = [{}] 的 beanDefinition", beanName);
            throw new NoSuchBeanDefinitionException(beanName);
        }

        //如果有个其他的线程正在 设置 bd 进入容器,这里就上锁,保证线程安全
        if (hasBeanCreationStarted()) {
            synchronized (this.beanDefinitionMap) {
                List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
                updatedDefinitions.remove(beanName);
                this.beanDefinitionNames = updatedDefinitions;
            }
        } else {
            this.beanDefinitionNames.remove(beanName);
        }
        //除了从容器里删除了 bd 如果这个 bd 已经生成了 单例对象,执行了一些后置处理器,这边都需要全部撤回
        resetBeanDefinition(beanName);
    }


    protected void resetBeanDefinition(String beanName) {
        //销毁单例对象
        destroySingleton(beanName);
        //执行移除 beanDefinition 的后置处理器
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            if (processor instanceof MergedBeanDefinitionPostProcessor) {
                ((MergedBeanDefinitionPostProcessor) processor).resetBeanDefinition(beanName);
            }
        }
    }

    /**
     * 删除单例对象
     *
     * @param beanName
     */
    public void destroySingleton(String beanName) {
        super.destroySingleton(beanName);
        clearByTypeCache();
    }

    private void clearByTypeCache() {
        this.allBeanNamesByType.clear();
        this.singletonBeanNamesByType.clear();
    }


    @Override
    public void registerAlias(String name, String alias) {
        Assert.hasText(name, "'name' 不能为空");
        Assert.hasText(alias, "'alias' 不能为空");
        synchronized (this.aliasMap) {
            //如果 别名等于真实的名字,那么从别名容器里把别名移除,就算没有也要尝试一下
            if (alias.equals(name)) {
                this.aliasMap.remove(alias);
                log.debug("别名 [{}] 和 真实的beanName [{}] 一样", alias, name);
            } else {
                String registeredName = this.aliasMap.get(alias);
                //如果已经存在了相同的别名,那么只会输出一下日志,不会覆盖注册
                if (registeredName != null) {
                    if (registeredName.equals(name)) {
                        //如果 这个别名已经注册过了,而且注册对象也是相同就直接跳过了.
                        return;
                    }
                    log.info("别名 [{}] 已经被 beanName [{}] 给注册了. 新注册的 beanName [{}] 讲无权使用该别名", alias,
                            registeredName, name);
                }
                //在spring 里这里还检查了 这个别名是否 循环依赖的问题,这边用其他方法解决这个问题.
                // 别名A --> 真名B(这个真名被当做了别名又被注册了一遍指向了别名A) --> 别名A
                //这里会直接 去 beanDefinitionNames 判断一遍,真名和别名不允许重名
                if (beanDefinitionNames.contains(alias)) {
                    String es = String.format("别名 [{}] 不能和beanName 重名 ", alias);
                    throw new BeanDefinitionStoreException(es);
                }
                this.aliasMap.put(alias, name);
                log.debug("别名 [{}] 已经和 beanName [{}] 关联", alias, name);
            }
        }
    }

    /**
     * 获取对应 name的所有别名
     *
     * @param name
     * @return
     */
    public String[] getAliases(String name) {
        List<String> result = new ArrayList<>();
        synchronized (this.aliasMap) {
            retrieveAliases(name, result);
        }
        return StringUtils.toStringArray(result);
    }

    private void retrieveAliases(String name, List<String> result) {
        this.aliasMap.forEach((alias, registeredName) -> {
            if (registeredName.equals(name)) {
                result.add(alias);
                retrieveAliases(alias, result);
            }
        });
    }


    /**
     * 如果已经存在了对应name的 beanDefinition 存在容器中，那么这个方法登场
     */
    private void beanDefinitionOverridingHandle(BeanDefinition oldBeanDefinition, String beanName,
                                                BeanDefinition beanDefinition) {
        //容器默认不给覆盖的话，直接异常
        if (!allowBeanDefinitionOverriding) {
            String es = String.format("不能注册这个 BeanDefinition [%s] : 因为对应的 beanName [%s]  已经存在 [%s] "
                    , beanDefinition, beanName, oldBeanDefinition);
            throw new BeanDefinitionStoreException(es);
        }
        //TODO 在原版spring里面这里BeanDefinition还有优先级的概念,低优先级不能覆盖高优先级,这里先忽略后面有需要填

        //覆盖者就是本尊,给个日志直接跳过了
        if (!beanDefinition.equals(oldBeanDefinition)) {
            log.info("重复注册了同一个 BeanDefinition [%s] , 对应的beanName 为 [%s]", beanDefinition, beanName);
            return;
        }
        //覆盖注册了
        this.beanDefinitionMap.put(beanName, beanDefinition);
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
        return StringUtils.toStringArray(this.beanDefinitionNames);
    }

    //======================AbstractBeanFactory 的实现方法=================================

    @Override
    public Comparator<Object> getDependencyComparator() {
        return this.dependencyComparator;
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
                } else {
                    //这里就直接初始化 单例对象了.
                    getBean(beanName);
                }
            }
        }

        //TODO 在 bean 生成单列对象后 , 如果实现了 SmartInitializingSingleton 接口,还会调用 这个接口去做一些后置处理的事情
    }


    /**
     * 获取合并后的  BeanDefinition
     * 这里不考虑 xml的逻辑,所以并不会去合并,只会生成 rootBeanDefinition
     * 这里有一层缓存
     *
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
        return getMergedBeanDefinition(beanName, beanDefinition, null);
    }


    /**
     * 用对应的 name 来判断一下 是不是  BeanFactory
     * 1 . 去直接拿他实例化后的对象 instanceof FactoryBean 来判断
     * 2 . 如果是非单列对象,这里要去拿 BeanDefinition 去判断
     *
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

    /**
     * 解析 依赖描述 对象,并且把这个 依赖的 单列对象给拿出来
     *
     * @param descriptor
     * @param requestingBeanName
     * @param autowiredBeanNames
     * @return
     * @throws BeansException
     */
    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName,
                                    Set<String> autowiredBeanNames) throws BeansException {

        Object result;
        result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames);
        return result;
    }

    /**
     * 去根据 依赖的 field/method 去寻找在IOC中可能的 bean对象
     * 这里优先使用 Class 类型匹配, 找不到会使用 名称 匹配对应的beanName
     * 同时还会做一些额外的操作来处理 当类型匹配到多个 bean对象的尴尬情况
     *
     * @param descriptor         存放 field/method 的对象
     * @param beanName           宿主 beanName
     * @param autowiredBeanNames 最终的 返回值是返回出一个 bean对象, 同时这里面会存放这个 bean对象的 beanName
     * @return
     * @throws BeansException
     */
    public Object doResolveDependency(DependencyDescriptor descriptor, String beanName,
                                      Set<String> autowiredBeanNames) throws BeansException {

        //获取了要注入的对象的类型
        Class<?> type = descriptor.getDependencyType();

        Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
        if (value != null) {
            if (value instanceof String) {
                //如果是 表达式，那么将会去配置文件里获取
                value = resolveEmbeddedValue((String) value);
            }
            //TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            //return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
            return value;
        }

        //去找一找有可能 依赖的 bean对象是什么
        Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
        //一个依赖的bean对象都没找到, 直接报错了
        if (matchingBeans.isEmpty()) {
            //如果设置了 required = true 那么 如果没有找到要注入对象是要抛异常的
            if (descriptor.isRequired()) {
                throw new NoSuchBeanDefinitionException("在bean [%s] 中注入属性 [%s] 失败,因为没有找到适合的注入对象....",
                        beanName, descriptor);
            }
            return null;
        }

        String autowiredBeanName;
        Object instanceCandidate;

        // 大于1说明找到了多个可以注入的对象,需要一定的筛选策略
        if (matchingBeans.size() > 1) {
            //去从多个 候选人里面去筛选出一个来注入
            autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
            //找不到适合的候选人
            if (autowiredBeanName == null) {
                // @Autowired(required=true) 代表一定要注入,但是现在我找到多个对象,没办法只好报错了
                if (descriptor.isRequired()) {
                    throw new NoUniqueBeanDefinitionException("在bean [%s] 中注入属性 [%s] 失败 因为有多个候选人,不知道用谁,候选人名单为[%s]",
                            beanName, descriptor, matchingBeans.keySet());
                }
                return null;
            }
            //找到候选人的名字,直接去拿对象了
            instanceCandidate = matchingBeans.get(autowiredBeanName);
        } else {
            //只有一个可候选对象直接拿了
            Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
            autowiredBeanName = entry.getKey();
            instanceCandidate = entry.getValue();
        }

        if (autowiredBeanNames != null) {
            autowiredBeanNames.add(autowiredBeanName);
        }

        //如果找到的候选人对象 还只是一个class,那么把他实例化了
        if (instanceCandidate instanceof Class) {
            instanceCandidate = getBean(autowiredBeanName);
        }

        Object result = instanceCandidate;
        //最后检查一波类型.
        if (!ClassUtils.isAssignableValue(type, result)) {
            throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
        }
        return result;
    }

    /**
     * 寻找可能注入的对象
     *
     * @param beanName
     * @param requiredType
     * @param descriptor
     * @return
     */
    protected Map<String, Object> findAutowireCandidates(
            String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

        //根据需要依赖的 class去寻找 他可能是哪些 beanName
        String[] candidateNames = BeanFactoryUtils
                .beanNamesForTypeIncludingAncestors(this, requiredType, true, descriptor.isEager());

        Map<String, Object> result = new LinkedHashMap<>(candidateNames.length);

        //找到了一堆 beanName, 筛选一下可能是谁
        for (String candidate : candidateNames) {
            //如果 找到的bean 不是自己依赖了自己,并且执行了 autowire 前置处理器, 通过前置处理器的考验才能继续注入
            //@Qualifler  注解的判断就是在 isAutowireCandidate 里面执行的
            if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
                //使用候选人的name 生成对应的对象,单例的话是 直接 getBean 拿了, 如果是非单例 放入进去的是 beanName 对应的 Class
                addCandidateEntry(result, candidate);
            }
        }
        return result;
    }

    /**
     * 获取 要注入进去的对象,并且把它放入 容器里
     *
     * @param candidates    结果容器
     * @param candidateName 要注入对象的 name
     *                      详情看方法 @findAutowireCandidates
     */
    private void addCandidateEntry(Map<String, Object> candidates, String candidateName) {

        if (containsSingleton(candidateName)) {
            Object beanInstance = getBean(candidateName);
            candidates.put(candidateName, beanInstance);
        } else {
            candidates.put(candidateName, getType(candidateName));
        }
    }


    /**
     * 判断 是不是 在bean 里面自己引用了自己
     *
     * @param beanName
     * @param candidateName
     * @return
     */
    private boolean isSelfReference(String beanName, String candidateName) {
        if (beanName == null || candidateName == null) {
            return false;
        }
        if (beanName.equals(candidateName)) {
            return true;
        }
        return false;
    }

    /**
     * 判断 beanName 对应的 类是不是 注入属性所需要的对象
     * 可以理解成 这就是  Autowire 的filter 在 注入之前先来执行一下, 可以打断注入
     * 比如 @Qualifier 注解的处理就是这里面执行的
     *
     * @param beanName
     * @param descriptor
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
            throws NoSuchBeanDefinitionException {
        return isAutowireCandidate(beanName, descriptor, getAutowireCandidateResolver());
    }

    protected boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver)
            throws NoSuchBeanDefinitionException {

        String beanDefinitionName = BeanFactoryUtils.transformedBeanName(beanName);
        if (containsBeanDefinition(beanDefinitionName)) {
            return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanDefinitionName), descriptor, resolver);
        } else if (containsSingleton(beanName)) {
            return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor, resolver);
        }
        return false;
    }


    protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
                                          DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {

        String beanDefinitionName = BeanFactoryUtils.transformedBeanName(beanName);
        resolveBeanClass(mbd);
        //拿到 要注入的候选人对象,这个 holder里面除了 封装了候选人的 bd外还放了所有的别名,用于等一下的判断
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(mbd, beanName, getAliases(beanDefinitionName));
        return resolver.isAutowireCandidate(beanDefinitionHolder, descriptor);
    }

    /**
     * 拿到了注入对象的 拿到了多个候选人 那么从这些候选人里选出最合适的一个,或者直接异常
     * 选择策略: 1 . 看谁打了@primary 注解,如果有多个打了这个注解 就异常
     * 2 . 比较属性的 名字是否和 候选人的beanName 一样
     *
     * @param candidates
     * @param descriptor
     * @return
     */
    protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
        Class<?> requiredType = descriptor.getDependencyType();
        //如果有候选人 打了 @primary 注解 走这个处理方法
        String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
        if (primaryCandidate != null) {
            return primaryCandidate;
        }

        //用注入属性的名字和候选人的beanName 比较,看有没有一模一样的,这里也会去比较别名
        for (Map.Entry<String, Object> entry : candidates.entrySet()) {
            String candidateName = entry.getKey();
            Object beanInstance = entry.getValue();
            if (matchesBeanName(candidateName, descriptor.getDependencyName())) {
                return candidateName;
            }

        }
        return null;
    }

    /**
     * 用来比较 beanName 的名字,这里会连别名一起比较
     *
     * @param beanName
     * @param candidateName
     * @return
     */
    protected boolean matchesBeanName(String beanName, String candidateName) {
        if (candidateName == null) {
            return false;
        }
        if (candidateName.equals(beanName)) {
            return true;
        }

        if (ObjectUtils.containsElement(getAliases(beanName), candidateName)) {
            return true;
        }
        return false;
    }


    /**
     * 注入属性的时候 用来处理 @primary 注解
     * 扫描所有的候选人,并且 去获取他们有没有 @primary 注解, 如果有多个人同时持有 @primary 注解 则会报错
     *
     * @param candidates
     * @param requiredType
     * @return
     */
    protected String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
        String primaryBeanName = null;
        for (Map.Entry<String, Object> entry : candidates.entrySet()) {
            String candidateBeanName = entry.getKey();
            Object beanInstance = entry.getValue();
            //判断准备注入的对象 里有没有 @primary 注解,有的话才继续
            if (isPrimary(candidateBeanName, beanInstance)) {
                if (primaryBeanName == null) {
                    primaryBeanName = candidateBeanName;
                } else {
                    //如果不等于Null,说明有多个 @primary 注解的候选人,那么抛出异常
                    throw new NoUniqueBeanDefinitionException("注入属性 [%s] 失败,因为有多个标注了 @primary 的候选人 [%s]->[%s]",
                            requiredType, primaryBeanName, candidateBeanName);
                }
            }
        }
        return primaryBeanName;

    }

    /**
     * 获取对应的 beanDefinition 来判断是否有 @primary 注解
     *
     * @param beanName
     * @param beanInstance
     * @return
     */
    protected boolean isPrimary(String beanName, Object beanInstance) {
        if (containsBeanDefinition(beanName)) {
            return getMergedLocalBeanDefinition(beanName).isPrimary();
        }
        return false;
    }


}
