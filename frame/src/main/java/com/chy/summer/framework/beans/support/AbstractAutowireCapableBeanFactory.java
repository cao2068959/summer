package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.*;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.beans.config.InstantiationAwareBeanPostProcessor;
import com.chy.summer.framework.beans.config.SmartInstantiationAwareBeanPostProcessor;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.core.DefaultParameterNameDiscoverer;
import com.chy.summer.framework.core.ParameterNameDiscoverer;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
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

    @Getter
    @Setter
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();


    protected InstantiationStrategy getInstantiationStrategy() {
        return this.instantiationStrategy;
    }

    /**
     * 创建对象
     *
     * @param beanName
     * @param mbd
     * @param args
     * @return
     */
    @Override
    protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) {
        log.debug("开始生成实例 {}", beanName);
        RootBeanDefinition mbdToUse = mbd;
        Class<?> resolvedClass = resolveBeanClass(mbdToUse);

        //其实 基本 99.99999% 是进不去这里的
        //如果真的进去了说明 class没有正确拿到,只能做一下clone 防止后面出什么问题了
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            //这里深拷贝,不污染入参
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
        }

        //执行了 bean 初始化前的前置处理器,这里如果是 aop的对象就会在这个前置过滤器执行的时候生成代理对象
        //这里如果是 factoryBean 还会计算出 真正的 工厂方法的返回类型,不过又塞入 bd里面了
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
        if (bean != null) {
            return bean;
        }

        //这里正真去创建对象了
        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        log.debug("实例 {} 创建完成", beanName);
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
            } catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "执行 MergedBeanDefinitionPostProcessor 处理器失败", ex);
            }
            mbd.postProcessed = true;
        }

        //如果是单例对象, 并且对应的bean已经开始初始化那么这么值就是true, singletonsCurrentlyInCreation 将会在 创建单例之前就把对应的beanName给存进去
        boolean earlySingletonExposure = (mbd.isSingleton() && isSingletonCurrentlyInCreation(beanName));
        //如果进这个if 那么意味着可能存在循环依赖的问题, 这里的解决方案也很简单, 把这个没初始化完成的bean变成一个 FactoryBean 注册进单例容器里面
        //后续流程再次获取 对应的bean的时候就不会直接去执行 创建bean 导致循序依赖,而是从这个 FactoryBean 里面拿到一个 未完成的bean, 继续后续流程
        //这里的 getEarlyBeanReference 除了会返回未完成的bean 还会去执行  BeanPostProcessors 的 SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference
        if (earlySingletonExposure) {
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
        } catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            } else {
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
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }

        try {
            //如果实现了 InitializingBean 接口去执行 bean的初始化方法
            invokeInitMethods(beanName, wrappedBean, mbd);
        } catch (Throwable throwable) {
            throw new BeanCreationException(
                    (mbd != null ? mbd.getResourceDescription() : null),
                    beanName, "Invocation of init method failed", throwable);
        }

        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }

        return wrappedBean;
    }


    protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
            throws Throwable {

        //如果实现接口 InitializingBean 则去执行 afterPropertiesSet 方法
        if (bean instanceof InitializingBean) {
            InitializingBean initializingBean = (InitializingBean) bean;
            initializingBean.afterPropertiesSet();
        }

        //TODO 在 beanDefinition 里还有保存了一个 initMethodName 字段,将会用反射区执行这个初始化方法,不过因为summer暂时搞全注解配置,所以用不上
    }


    /**
     * 调用BeanPostProcessor后置处理器实例对象初始化之前的处理方法
     *
     * @param existingBean
     * @param beanName
     * @return
     * @throws BeansException
     */
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
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


    /**
     * 1. 帮 RootBeanDefinition 选择出来谁才是真正需要实例化的类
     * 2. 执行了 bean 初始化前的前置处理器,这里如果是 aop的对象就会在这个前置过滤器执行的时候生成代理对象
     * 3. 如果 第2步所说的前置处理器已经生成了 实例对象,那么执行 bean 创建后的后置处理器
     *
     * @param beanName
     * @param mbd
     * @return
     */
    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
            if (!mbd.isSynthetic()) {
                //决定出谁才是真正要实例化的类
                Class<?> targetType = determineTargetType(beanName, mbd);
                if (targetType != null) {
                    //执行创建bean之前的 前置处理器,这里aop会在这里去创建 代理对象
                    bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                    if (bean != null) {
                        //如果前置处理器已经生成实例化对象,那么执行 bean创建完成后的 后置处理器
                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                    }
                }
            }
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
    }

    /**
     * 去确定 一下 在 bd里面真正的目标类是什么(目标类:会真正实例化的类型)
     * 普通的 bean 确实目标类直接拿 targetType 就行了
     * 有普通就有特殊 比如 factoryBean 以及 @bean 标注过的 这些 bean的类型需要计算后才能获得
     *
     * @param beanName
     * @param mbd
     * @param typesToMatch
     * @return
     */
    @Nullable
    protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if (targetType != null) {
            return targetType;
        }

        //如果这个 bd里设置有了 工厂方法 那么就准备
        if (mbd.getFactoryMethodName() != null) {
            targetType = getTypeForFactoryMethod(beanName, mbd, typesToMatch);
        } else {
            targetType = resolveBeanClass(mbd);
        }

        if (ObjectUtils.isEmpty(typesToMatch)) {
            mbd.resolvedTargetType = targetType;
        }
        return targetType;
    }


    /**
     * 获取工厂类的返回值的类型,主要针对 @bean 的方法
     *
     * @param beanName
     * @param mbd
     * @param typesToMatch
     * @return
     */
    protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
        if (cachedReturnType != null) {
            return cachedReturnType.resolve();
        }

        Class<?> factoryClass = null;
        boolean isStatic = true;

        String factoryBeanName = mbd.getFactoryBeanName();
        if (factoryBeanName != null) {
            if (factoryBeanName.equals(beanName)) {
                throw new BeanDefinitionStoreException("factoryBean 的name不能和 持有者的name 一致");
            }
            factoryClass = getType(factoryBeanName);
            isStatic = false;
        }

        //这里还是 null那走不下去 直接返回null吧
        if (factoryClass == null) {
            return null;
        }

        //如果被 动态代理了就拿被代理的class出来
        factoryClass = ClassUtils.getUserClass(factoryClass);


        Class<?> commonType = null;
        Method uniqueCandidate = null;

        //获取目标类的最少构造参数 的数量
        int minNrOfArgs = (mbd.hasConstructorArgumentValues() ?
                mbd.getConstructorArgumentValues().getArgumentCount() : 0);
        //反射拿到目标类里面所有的方法
        Method[] candidates = ReflectionUtils.getUniqueDeclaredMethods(factoryClass);
        for (Method candidate : candidates) {
            //如果这个方法的 方法类型(对象方法/静态方法) 和 工厂方法的不同，就直接下一个
            if (Modifier.isStatic(candidate.getModifiers()) != isStatic) {
                continue;
            }
            //这里比较了 工厂方法的名字，和 目标方法是否一致
            if (!mbd.isFactoryMethod(candidate)) {
                continue;
            }
            //这里比较了方法参数的个数是否一致（因为可能会存在 可变参数的参数 所以 工厂方法的实际参数 少于 目标方法也是可以接受的）
            if (candidate.getParameterCount() < minNrOfArgs) {
                continue;
            }

            //如果有泛型
            if (candidate.getTypeParameters().length > 0) {
                //TODO 如果方法的返回值是 泛型,这里先留一个坑
            } else {
                //如果遍历的时候找到了 多个方法(方法名字相同,但是参数不同的),这个参数就会变成
                uniqueCandidate = (commonType == null ? candidate : null);
                //获取 bean方法返回值的类型,如果找到了多个 同名的方法,那么比较后取子类 子类,如果多个方法的返回值类型都不同,那么直接返回null
                commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
                //返回了Null,说明 方法的返回值类型，不匹配
                if (commonType == null) {
                    return null;
                }
            }

        }

        if (commonType == null) {
            return null;
        }

        //在spring 里这里可能还会用 uniqueCandidate 去解析出返回值类型,而这里直接使用上面返回出来的返回值类型
        cachedReturnType = ResolvableType.forClass(commonType);
        mbd.factoryMethodReturnType = cachedReturnType;
        return cachedReturnType.resolve();
    }


    /**
     * 在 bean 在创建之前 调用的后置处理器 ,后置处理器的类型是 InstantiationAwareBeanPostProcessor
     *
     * @param beanClass
     * @param beanName
     * @return
     */
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
     *
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
                ((BeanFactoryAware) bean).setBeanFactory(this);
            }
        }
    }

    private void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper instanceWrapper) {

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
     * @param beanName
     * @param mbd
     * @param bean
     * @return
     * @see #doCreateBean(String, RootBeanDefinition, Object[])  在这方法上设置进入这个方法
     * @see DefaultSingletonBeanRegistry#getSingleton(String beanName, boolean allowEarlyReference)
     * 当第二个参数是true的时候会调用这个方法来获取单例对象
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
     *
     * @param beanName
     * @param mbd
     * @param args
     * @return
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {

        Class<?> beanClass = resolveBeanClass(mbd);

        //判断类是否 是public 修饰
        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers())) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "这个类并没有用 public 修饰类不能实例化: " + beanClass.getName());
        }

        //TODO 这里还有使用 jdk8的 Supplier 去创建对象,这里先留坑

        //如果是 用工厂方法区创建 instantiate 的话走这里, @Bean 创建对象的逻辑
        if (mbd.getFactoryMethodName() != null) {
            return instantiateUsingFactoryMethod(beanName, mbd, args);
        }

        //去找到目标 class 中的构造方法
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        if (ctors != null) {
            //通过构造器去创建对象
            return autowireConstructor(beanName, mbd, ctors, args);
        }

        //如果没有定义构造函数,那么就直接使用 无参构造器去生成实例对象
        return instantiateBean(beanName, mbd);
    }

    /**
     * 通过构造器去 创建对象
     *
     * @param beanName
     * @param mbd
     * @param ctors
     * @param explicitArgs
     * @return
     */
    protected BeanWrapper autowireConstructor(
            String beanName, RootBeanDefinition mbd, @Nullable Constructor<?>[] ctors, @Nullable Object[] explicitArgs) {

        return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
    }


    /**
     * 通过 BeanPostProcessor 去找到有参构造器,这里默认工作的实际上是 @AutowiredAnnotationBeanPostProcessor
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(@Nullable Class<?> beanClass, String beanName)
            throws BeansException {
        if (beanClass == null) {
            return null;
        }

        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
                if (ctors != null) {
                    return ctors;
                }
            }
        }
        return null;
    }


    /**
     * 通过 工厂方法(@Bean) 来实例化 bean 对象的
     *
     * @param beanName
     * @param mbd
     * @param explicitArgs
     * @return
     */
    protected BeanWrapper instantiateUsingFactoryMethod(
            String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {

        return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
    }

    /**
     * 无参数的构造器初始化
     *
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
        } catch (Throwable ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "实例化bean失败", ex);
        }
    }

    protected void initBeanWrapper(BeanWrapper bw) {
    }

    public abstract Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName,
                                             Set<String> autowiredBeanNames) throws BeansException;

    public abstract void registerDependentBean(String beanName, String dependentBeanName) throws ClassNotFoundException;

}
