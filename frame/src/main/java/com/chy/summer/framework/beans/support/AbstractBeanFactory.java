package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.HierarchicalBeanFactory;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.exception.*;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.chy.summer.framework.util.BeanFactoryUtils.transformedBeanName;

public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements HierarchicalBeanFactory {

    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private BeanFactory parentBeanFactory;

    protected final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

    /**
     * 判断是否已经开始创建 bean 对象了
     * @return
     */
    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

    public abstract Comparator<Object> getDependencyComparator();



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
                }
                else {
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

    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd){
        try {
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            return doResolveBeanClass(mbd);
        }
        catch (ClassNotFoundException | LinkageError e) {
            e.printStackTrace();
            throw new CannotLoadBeanClassException("类解析失败 [{}]",mbd);
        }
    }

    private Class<?> doResolveBeanClass(RootBeanDefinition mbd)
            throws ClassNotFoundException {
        return mbd.resolveBeanClass(ClassUtils.getDefaultClassLoader());
    }


    protected abstract RootBeanDefinition getMergedLocalBeanDefinition(String beanName);

    protected abstract boolean containsBeanDefinition(String beanName);


    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        return factoryBean.getObjectType();
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

    @Override
    @Nullable
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        // 尝试拿一下单列对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            //如果拿到了,那么看看是不是 FactoryBean 是的话走 FactoryBean 的专用方法,不是就直接给类型
            if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
            }
            else {
                return beanInstance.getClass();
            }
        }

        // 如果不是单例对象,那么从BeanDefinition 上入手,这里
        //TODO 先留坑
        return null;
    }


    @Override
    @Nullable
    public BeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }


    /**
     * 返回一个合并后的 BeanDefinition,如果对应的 Bean 存在父子关系的话
     * 因为这个项目主要是 已经注解的方式为主导,这边beanDefinition 的父子继承并不是使用太多,这边就先不填这个坑
     * @param beanName
     * @param bd
     * @param containingBd
     * @return
     * @throws BeanDefinitionStoreException
     */
    protected RootBeanDefinition getMergedBeanDefinition(
            String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
            throws BeanDefinitionStoreException {

        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;

            //这里再检查一次,防止上锁之前
            if (containingBd == null) {
                mbd = this.mergedBeanDefinitions.get(beanName);
            }

            if (mbd == null) {
                //先看要合并的 BeanDefinition 有没有父 bd ,如果没有就是直接封装进去 RootBeanDefinition
                if (bd.getParentName() == null) {
                    // 如果已经是 RootBeanDefinition 对象,那么深度拷贝出来
                    if (bd instanceof RootBeanDefinition) {
                        mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                    }
                    else {
                        mbd = new RootBeanDefinition(bd);
                    }
                } else {
                    //TODO 下面开始是 如果是有父 BeanDefinition 的情况下
                }
            }
            //写入缓存
            this.mergedBeanDefinitions.put(beanName,mbd);
            return mbd;
        }
    }


    @Override
    public boolean containsBean(String beanName) {
        return false;
    }

    public <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args,
                           boolean typeCheckOnly)throws BeansException{
        Object bean = null;
        final String beanName = transformedBeanName(name);
        //先去单例的缓存里看看有没有对应的对象
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            //虽然已经拿到了单列对象，但是这个对象可以能还没初始化完成
            // 如果是FactoryBean,需要在这个方法里去调用 getObject 方法来获取真正的对象
            // 如果用户传入的 name 前面带了 & 那么这里会直接 把FactoryBean 对象给返回出去，而去调用 getObject
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        }else{
            //获取这个beanName 对应的 beanDefintion
            final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
            //这里检查 对应的beanDefintion 是不是抽象类,抽象类不能实例化,抛出异常
            checkMergedBeanDefinition(mbd,beanName);
            //开始使用 BeanDefinition 去创建对象
            creatObjectForBeanDefinition(name,beanName,mbd,args);
        }

        //如果 指定了生成的类型,但是拿到的实例和类型不符合进入这个if
        if (requiredType != null && !requiredType.isInstance(bean)){
            //我这里也懒得处理,直接异常
            throw new BeanNotOfRequiredTypeException("name:[%s] 对应的实例,和指定类 [%s] 类型不匹配",name,requiredType);
        }

        return (T)bean;
    }


    /**
     * 通过 BeanDefinition 去创建对象
     * @param name
     * @param beanName
     * @param mbd
     * @param args
     * @return
     */
    private Object creatObjectForBeanDefinition(String name ,String beanName,RootBeanDefinition mbd,final Object[] args){

        Object result = null;
        if (mbd.isSingleton()) {
            //这里真正开始创建单例哦
            result = getSingleton(beanName, () -> {
                try {
                    return createBean(beanName, mbd, args);
                }
                catch (BeansException ex) {
                    throw ex;
                }
            });
        }else{
            //非单例创建
            result = createBean(beanName, mbd, args);
        }

        //同doGetBean 方法,虽然拿到了对象,但是还是要对 FactoryBean 做一下处理
        return getObjectForBeanInstance(result, name, beanName, mbd);
    }


    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args);

    /**
     * 为 FactoryBean 创建对象的方法
     * @param beanInstance
     * @param name
     * @param beanName
     * @param mbd
     * @return
     */
    protected Object getObjectForBeanInstance(
            Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd){

        //如果name 传入了 &xxx 的格式，但是实际上并不是 FactoryBean，感觉被欺骗了，生气的抛出异常
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            if (!(beanInstance instanceof FactoryBean)) {
                throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
            }
        }

        //这里如果他不是 FactoryBean 或者 他就是想要 FactoryBean 本身的对象（&name 的模式），那么就直接返回
        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
            return beanInstance;
        }

        //也先去缓存里看看 有没人已经生成了
        Object result = getCachedObjectForFactoryBean(beanName);
        if(result !=null){
            return result;
        }

        FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
        //从factroyBean 里去调用 getObjetc 方法了
        return getObjectFromFactoryBean(factory, beanName);

    }

    protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName)
            throws BeanDefinitionStoreException {

        if (mbd.isAbstract()) {
            throw new BeanIsAbstractException("类 : [%s] 是抽象类,不能实例化",beanName);
        }
    }
}
