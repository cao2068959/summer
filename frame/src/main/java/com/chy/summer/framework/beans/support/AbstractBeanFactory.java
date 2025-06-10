package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.HierarchicalBeanFactory;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.core.StringValueResolver;
import com.chy.summer.framework.exception.*;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;


import java.lang.IllegalStateException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.chy.summer.framework.util.BeanFactoryUtils.transformedBeanName;

@Slf4j
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements HierarchicalBeanFactory {

    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private BeanFactory parentBeanFactory;

    protected final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

    private final List<StringValueResolver> embeddedValueResolvers = new CopyOnWriteArrayList<>();
    /**
     * 判断是否已经开始创建 bean 对象了
     *
     * @return
     */
    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

    public abstract Comparator<Object> getDependencyComparator();


    /**
     *
     * 匹配 beanName 对应的 实例 和 typeToMatch 传入进来的 class 是否匹配 或者 是继承关系
     *
     * 单列 : 调用 getSingleton 去获取实例对象 然后去匹配对应的class
     * 非单列: 获取BeanDefinition 然后拿到 class去对比
     *
     *
     * @param name
     * @param typeToMatch
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    @Override
    public boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        //获取单列对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                //如果他实现了 FactoryBean 但是又不是 FactoryBean 的bean,那么就直接调用 FactoryBean的 getObjectType 来获取类型
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);
                    //判断typeToMatch 是否是 type 的父类
                    return (type != null && typeToMatch.isAssignableFrom(type));
                } else {
                    //判断 typeToMatch 和 实例的关系
                    return typeToMatch.isInstance(beanInstance);
                }
            } else if (!BeanFactoryUtils.isFactoryDereference(name)) {
                if (typeToMatch.isInstance(beanInstance)) {
                    return true;
                }
                //TODO 这里还有泛型的判断,这里先留个坑
            }
            return false;

        } else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            //这里如果beanName 是一个单例 但是没有对应的 BeanDefinition 那么 就直接跳过了
            return false;
        }

        //下面是非单列对象, 那么去拿他的 BeanDefinition 然后去判断
        //去拿到对应name的 BeanDefinition
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        Class<?> beanType = predictBeanType(mbd);
        if (beanType == null) {
            return false;
        }
        return typeToMatch.isAssignableFrom(beanType);
    }

    protected Class<?> predictBeanType(RootBeanDefinition mbd) {
        Class<?> targetType = mbd.getTargetType();
        if (targetType != null) {
            return targetType;
        }
        return resolveBeanClass(mbd);
    }

    /**
     * 获取 beanDefinition 里面 源类的 class
     * @param mbd
     * @return
     */
    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd) {
        try {
            //没找到 beanClass 并不是代表 beanClass 属性没有值,而是可能 beanClass 里面是string类型的全路径,而不是 class类型
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            //把 string类型的全路径给转成 class类型
            return doResolveBeanClass(mbd);
        } catch (ClassNotFoundException | LinkageError e) {
            e.printStackTrace();
            throw new CannotLoadBeanClassException("类解析失败 [{}]", mbd);
        }
    }

    /**
     * 把 beanDefinition 里的beanClass 值所记录的源bean 的全路径 转成 class类型
     * @param mbd
     * @return
     * @throws ClassNotFoundException
     */
    private Class<?> doResolveBeanClass(RootBeanDefinition mbd)
            throws ClassNotFoundException {
        return mbd.resolveBeanClass(ClassUtils.getDefaultClassLoader());
    }


    protected abstract RootBeanDefinition getMergedLocalBeanDefinition(String beanName);

    protected abstract boolean containsBeanDefinition(String beanName);


    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        return factoryBean.getObjectType();
    }

    protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
        if (!mbd.isSingleton()) {
            return null;
        }
        try {
            FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null);
            return getTypeForFactoryBean(factoryBean);
        } catch (BeanCreationException ex) {
            log.warn("生成对应的 factoryBean [{}] 失败", beanName);
            return null;
        }
    }

    public void setParentBeanFactory(BeanFactory parentBeanFactory) {
        this.parentBeanFactory = parentBeanFactory;
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "BeanPostProcessor 必须不能为 null");
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    @Override
    public boolean containsLocalBean(String name) {
        String beanName = transformedBeanName(name);
        return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
                (!BeanFactoryUtils.isFactoryDereference(name)));
    }

    public void addEmbeddedValueResolvers(StringValueResolver stringValueResolver) {
        embeddedValueResolvers.add(stringValueResolver);
    }

    /**
     * 通过beanName 获取对应的类型
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    @Override
    @Nullable
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        // 尝试拿一下已经生成好的单列对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            //如果拿到了,那么看看是不是 FactoryBean 是的话走 FactoryBean 的专用方法,不是就直接给类型
            if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
            } else {
                return beanInstance.getClass();
            }
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
        if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
            RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
            Class<?> targetClass = predictBeanType(tbd);
            if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
                return targetClass;
            }
        }

        Class<?> beanClass = predictBeanType(mbd);

        //如果是属于 FactoryBean 的走下面
        if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
            if (!BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean(beanName, mbd);
            } else {
                return beanClass;
            }
        }

        //只是普通的class 直接返回
        return (!BeanFactoryUtils.isFactoryDereference(name) ? beanClass : null);

    }


    @Override
    @Nullable
    public BeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }


    /**
     * 返回一个合并后的 BeanDefinition,如果对应的 Bean 存在父子关系的话
     * 非xml配置的 BeanDefinition 是没有这玩意的,这里就不去做处理了,直接返回
     *
     * @param beanName
     * @param bd
     * @param containingBd
     * @return
     * @throws BeanDefinitionStoreException
     */
    protected RootBeanDefinition getMergedBeanDefinition(
            String beanName, BeanDefinition bd, BeanDefinition containingBd)
            throws BeanDefinitionStoreException {
        Assert.notNull(bd, "beanDefinition 不能为Null,可能 [" + beanName + "] 没有注册进容器");
        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;
            //这里再检查一次,防止上锁之前有值进来了
            if (containingBd == null) {
                mbd = this.mergedBeanDefinitions.get(beanName);
            }

            if (bd instanceof RootBeanDefinition) {
                mbd = (RootBeanDefinition) bd;
            } else {
                mbd = new RootBeanDefinition(bd);
            }
            //写入缓存
            this.mergedBeanDefinitions.put(beanName, mbd);
            return mbd;
        }
    }


    @Override
    public boolean containsBean(String beanName) {
        return false;
    }


    @Override
    public Object getBean(String name) {
        return doGetBean(name, null, null);
    }

    @Override
    public <T> T getBean(String name, Class<T> type) {
        return doGetBean(name, type, null);
    }


    public <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args) throws BeansException {
        Object bean = null;
        final String beanName = transformedBeanName(name);
        //先去单例的缓存里看看有没有对应的对象, 如果是循环依赖的情况下这里拿到的可能是一个半成品的单例对象
        Object sharedInstance = getSingleton(beanName, true);
        if (sharedInstance != null && args == null) {
            //虽然已经拿到了单列对象，但是这个对象可以能还没初始化完成
            // 如果是FactoryBean,需要在这个方法里去调用 getObject 方法来获取真正的对象
            // 如果用户传入的 name 前面带了 & 那么这里会直接 把FactoryBean 对象给返回出去，而去调用 getObject
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        } else {
            //TODO 这里在原本的spring里会先去 父ioc容器里调用一下 getBean 来尝试获取一下,这里先不考虑父子ioc容器

            //获取这个beanName 对应的 beanDefintion
            final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
            //这里检查 对应的beanDefintion 是不是抽象类,抽象类不能实例化,抛出异常
            checkMergedBeanDefinition(mbd, beanName);
            //开始使用 BeanDefinition 去创建对象
            bean = creatObjectForBeanDefinition(name, beanName, mbd, args);
        }

        //如果 指定了生成的类型,但是拿到的实例和类型不符合进入这个if
        if (requiredType != null && !requiredType.isInstance(bean)) {
            //我这里也懒得处理,直接异常
            throw new BeanNotOfRequiredTypeException("name:[%s] 对应的实例,和指定类 [%s] 类型不匹配", name, requiredType);
        }

        return (T) bean;
    }


    /**
     * 通过 BeanDefinition 去创建对象
     *
     * @param name
     * @param beanName
     * @param mbd
     * @param args
     * @return
     */
    private Object creatObjectForBeanDefinition(String name, String beanName, RootBeanDefinition mbd, final Object[] args) {

        Object result = null;
        if (mbd.isSingleton()) {
            //这里真正开始创建单例哦
            //这里的getSingleton 会去拿单例对象,拿不到的话就会走 这个匿名方法 创建对象,然后存入单例容器
            result = getSingleton(beanName, () -> {
                try {
                    //我就是楼上说的那个创建方法
                    return createBean(beanName, mbd, args);
                } catch (BeansException ex) {
                    throw ex;
                }
            });
        } else {
            //非单例创建
            result = createBean(beanName, mbd, args);
        }

        //同doGetBean 方法,虽然拿到了对象,但是还是要对 FactoryBean 做一下处理
        return getObjectForBeanInstance(result, name, beanName, mbd);
    }


    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args);

    /**
     * 为 FactoryBean 创建对象的方法
     *
     * @param beanInstance
     * @param name
     * @param beanName
     * @param mbd
     * @return
     */
    protected Object getObjectForBeanInstance(
            Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

        //如果name 传入了 &xxx 的格式 则进这个逻辑
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            //虽然 name 传入了 &xxx 的格式，但是实际上并不是 FactoryBean，感觉被欺骗了，生气的抛出异常
            if (!(beanInstance instanceof FactoryBean)) {
                throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
            }
            //直接把 FactoryBean 对象给返回出去了
            return beanInstance;
        }

        //他不是 FactoryBean 类型直接返回出去
        if (!(beanInstance instanceof FactoryBean)) {
            return beanInstance;
        }

        //也先去缓存里看看 有没人执行过 FactoryBean 已经生成了对应的对象
        Object result = getCachedObjectForFactoryBean(beanName);
        if (result != null) {
            return result;
        }

        FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
        //从factroyBean 里去调用 getObjetc 方法了, 如果是单例生成后 放入 factoryBeanObjectCache 缓存里
        return getObjectFromFactoryBean(factory, beanName);

    }

    protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName)
            throws BeanDefinitionStoreException {

        if (mbd.isAbstract()) {
            throw new BeanIsAbstractException("类 : [%s] 是抽象类,不能实例化", beanName);
        }
    }

    /**
     * 解析嵌入的表达式 ，比如 ${abc} 这样的值
     *
     * @param value
     * @return
     */
    public String resolveEmbeddedValue(String value){
        if (value == null) {
            return null;
        }
        String result = value;
        for (StringValueResolver resolver : this.embeddedValueResolvers) {
            result = resolver.resolveStringValue(result);
            if (result == null) {
                return null;
            }
        }
        return result;
    }

}
