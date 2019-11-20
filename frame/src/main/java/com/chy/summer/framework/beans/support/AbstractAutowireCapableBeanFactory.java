package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.*;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.beans.config.InstantiationAwareBeanPostProcessor;
import com.chy.summer.framework.beans.config.SmartInstantiationAwareBeanPostProcessor;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
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

        //aop 替换
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
        if (bean != null) {
            return bean;
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
        //如果会面临循环依赖的问题,那么 把这个半成品的bean 给放入单例容器里面
        if (earlySingletonExposure) {
            //getEarlyBeanReference 这个其实就是直接返回入参的bean对象,只是在返回的时候会执行一下 半成品的后置处理器(如果存在的话)
            addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
        }

        Object exposedObject = bean;
        try {
            //同样的 BeanPostProcessor执行器,这里是  InstantiationAwareBeanPostProcessor
            //这个执行器的主要功能在于 属性的注入
            populateBean(beanName, mbd, instanceWrapper);
            //这里也是 BeanPostProcessor执行器 ,但是这次是真的执行了所有 BeanPostProcessor 接口
            //同时也执行了所有的 Aware 接口
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

    private Object initializeBean(String beanName, Object bean, RootBeanDefinition mbd) {

        //执行对应的 Aware 接口
        invokeAwareMethods(beanName, bean);
        Object wrappedBean = bean;
        //对BeanPostProcessor后置处理器的postProcessAfterInitialization
        //回调方法的调用，为Bean实例初始化之后做一些处理
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
        return wrappedBean;
    }

    /**
     * 调用BeanPostProcessor后置处理器实例对象初始化之后的处理方法
     */
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        //遍历容器为所创建的Bean添加的所有BeanPostProcessor后置处理器
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            //调用Bean实例所有的后置处理中的初始化后处理方法，为Bean实例对象在
            //初始化之后做一些自定义的处理操作
            Object current = beanProcessor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
            // Make sure bean class is actually resolved at this point.
            if (!mbd.isSynthetic()) {
                Class<?> targetType = determineTargetType(beanName, mbd);
                if (targetType != null) {
                    bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                    if (bean != null) {
//                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                    }
                }
            }
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
    }

    @Nullable
    protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if (targetType == null) {
            targetType = resolveBeanClass(mbd);
            if (ObjectUtils.isEmpty(typesToMatch)) {
                mbd.resolvedTargetType = targetType;
            }
        }
        return targetType;
    }

    //调用BeanPostProcessor后置处理器实例对象初始化之前的处理方法
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 执行对应的 Aware 接口
     * @param beanName
     * @param bean
     */
    private void invokeAwareMethods(final String beanName, final Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            if (bean instanceof BeanClassLoaderAware) {
                ClassLoader bcl = ClassUtils.getDefaultClassLoader();
                if (bcl != null) {
                    ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
                }
            }
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }

    private void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper instanceWrapper) {
        PropertyDescriptor[] filteredPds = null;
        PropertyValues pvs = mbd.getPropertyValues();
        //在spring中，这里还有一个开关来控制是否有对应的后置处理器，来控制是否执行下面的 for循环，这里就补设置这个开关
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                //执行 后置处理器 来给bean的属性做一些操作
                //其中属性注入的 后置处理器 为：AutowiredAnnotationBeanPostProcessor
                PropertyValues pvsToUse = ibp.postProcessProperties(pvs, instanceWrapper.getWrappedInstance(), beanName);
                if (pvsToUse == null) {
                    return;
                }
                pvs = pvsToUse;
            }
        }
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
     * 获取半成品的单例对象,并且执行了半成品的后置处理器,这是方法是
     *
     * @see #doCreateBean(String, RootBeanDefinition, Object[])  在这方法上设置进入这个方法
     *
     * @see DefaultSingletonBeanRegistry#getSingleton(String beanName, boolean allowEarlyReference)
     * 当第二个参数是true的时候会调用这个方法来获取单例对象
     *
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
