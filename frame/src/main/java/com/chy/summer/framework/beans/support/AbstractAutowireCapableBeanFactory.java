package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanWrapper;
import com.chy.summer.framework.beans.BeanWrapperImpl;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 用来做实例化类的 BeanFactory
 */
@Slf4j
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {


    /**
     * 存放没有初始化完成的 factoryBean
     */
    private final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>(16);

    private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();


    protected InstantiationStrategy getInstantiationStrategy() {
        return this.instantiationStrategy;
    }

    /**
     * 创建对象
     * @param beanName
     * @param mbd
     * @param args
     * @return
     */
    @Override
    protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) {
        log.debug("开始生成实例 {}",beanName);
        RootBeanDefinition mbdToUse = mbd;
        Class<?> resolvedClass = resolveBeanClass(mbdToUse);
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            //这里深拷贝,不污染入参
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
        }

        //这里正真去创建对象了
        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        log.debug("实例 {} 创建完成",beanName);
        return beanInstance;
    }

    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args)
            throws BeanCreationException {

        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }

        //用反射去创建了 对象，并且把它放入 wrapper 中
        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }

        //获取生成好的 对象
        final Object bean = instanceWrapper.getWrappedInstance();
        Class<?> beanType = instanceWrapper.getWrappedClass();

        //把实例的类型 直接设入 RootBeanDefinition
        mbd.resolvedTargetType = beanType;

        if (!mbd.postProcessed) {
            try {
                // 执行了bean的前置处理器 也就是 BeanPostProcessor ,这里先只执行 MergedBeanDefinitionPostProcessor 类型
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "执行 MergedBeanDefinitionPostProcessor 处理器失败", ex);
            }
            mbd.postProcessed = true;
        }

        //是否面临了循环依赖的问题
        //如果是正在创建中的单例对象，可能会有循环依赖问题
        boolean earlySingletonExposure = (mbd.isSingleton()  && isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            //这个getEarlyBeanReference 其实也是一个BeanPostProcessor 执行器,这里执行的是接口 SmartInstantiationAwareBeanPostProcessor
            //这里也是循环依赖的破解关键
            addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
        }

        Object exposedObject = bean;
        try {
            //同样的 BeanPostProcessor执行器,这里是  InstantiationAwareBeanPostProcessor
            //这个执行器的主要功能在于 属性的注入
            populateBean(beanName, mbd, instanceWrapper);
            //这里也是 BeanPostProcessor执行器 ,但是这次是真的执行了所有 BeanPostProcessor 接口
            exposedObject = initializeBean(beanName, exposedObject, mbd);
        }
        catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            }
            else {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

        return exposedObject;
    }

    private Object initializeBean(String beanName, Object exposedObject, RootBeanDefinition mbd) {
        return exposedObject;
    }

    private void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper instanceWrapper) {

    }

    private void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof MergedBeanDefinitionPostProcessor) {
                MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
                bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
            }
        }
    }


    /**
     * 执行 BeanPostProcessor 不过是 SmartInstantiationAwareBeanPostProcessor 的执行器
     * @param beanName
     * @param mbd
     * @param bean
     * @return
     */
    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
            }
        }
        return exposedObject;
    }

    /**
     * 实例化对象
     * @param beanName
     * @param mbd
     * @param args
     * @return
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd,  Object[] args) {

        Class<?> beanClass = resolveBeanClass(mbd);

        //判断类是否 是public 修饰
        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers())) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "这个类并没有用 public 修饰类不能实例化: " + beanClass.getName());
        }

        //TODO 这里还有使用 jdk8的 Supplier 去创建对象,这里先留坑

        //这里直接用无参构造器去初始化 对象,同时用 BeanWrapper 给包装了
        return instantiateBean(beanName, mbd);
    }

    /**
     * 无参数的构造器初始化
     * @param beanName
     * @param mbd
     * @return
     */
    protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
        try {
            Object beanInstance;
            final BeanFactory parent = this;
            //用无参构造器实例化了
            beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
            BeanWrapper bw = new BeanWrapperImpl(beanInstance);
            //初始化一些属性编辑器的东西,这里先忽略
            initBeanWrapper(bw);
            return bw;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "实例化bean失败", ex);
        }
    }

    private void initBeanWrapper(BeanWrapper bw) {
    }

}
