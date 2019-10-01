package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.*;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.core.cglib.SummerNamingPolicy;
import net.sf.cglib.proxy.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.core.SmartClassLoader;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.sun.istack.internal.Nullable;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.*;
import net.sf.cglib.transform.impl.UndeclaredThrowableStrategy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

/**
 * AOP框架的基于CGLIB的AopProxy实现。
 */
class CglibAopProxy implements AopProxy, Serializable {

    /**
     * CGLIB回调数组索引的常量
     */
    private static final int AOP_PROXY = 0;
    private static final int INVOKE_TARGET = 1;
    private static final int NO_OVERRIDE = 2;
    private static final int DISPATCH_TARGET = 3;
    private static final int DISPATCH_ADVISED = 4;
    private static final int INVOKE_EQUALS = 5;
    private static final int INVOKE_HASHCODE = 6;


//	/** Logger available to subclasses; static to optimize serialization */
//	protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);

    /**
     * 保存对类验证的结果
     */
    private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();


    /**
     * 此代理的配置
     */
    protected final AdvisedSupport advised;

    @Nullable
    protected Object[] constructorArgs;

    @Nullable
    protected Class<?>[] constructorArgTypes;

    /**
     * 用于dispatcher的advice方法
     */
    private final transient AdvisedDispatcher advisedDispatcher;

    /**
     * 固定回调，方法与回调的映射关系
     */
    private transient Map<String, Integer> fixedInterceptorMap = Collections.emptyMap();

    /**
     * 固定拦截器偏移量
     * 在getCallbacks方法中会将fixedCallbacks接在mainCallbacks之后形成新的Callbacks返回，偏移量所有指的位置就是fixedCallbacks的第一个callback
     */
    private transient int fixedInterceptorOffset;


    /**
     * 为给定的AOP配置创建一个新的CglibAopProxy
     */
    public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport不可为空");
        if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
            throw new AopConfigException("没有advisors，也没有指定TargetSource");
        }
        this.advised = config;
        this.advisedDispatcher = new AdvisedDispatcher(this.advised);
    }

    /**
     * 设置构造函数参数以用于创建代理
     *
     * @param constructorArgs     构造函数参数值
     * @param constructorArgTypes 构造函数参数类型
     */
    public void setConstructorArguments(@Nullable Object[] constructorArgs, @Nullable Class<?>[] constructorArgTypes) {
        if (constructorArgs == null || constructorArgTypes == null) {
            throw new IllegalArgumentException("需要同时指定“ constructorArgs”和“ constructorArgTypes”");
        }
        if (constructorArgs.length != constructorArgTypes.length) {
            throw new IllegalArgumentException("'constructorArgs'的长度(" + constructorArgs.length +
                    ") 必须与'constructorArgTypes'的长度 (" + constructorArgTypes.length + ")相等");
        }
        this.constructorArgs = constructorArgs;
        this.constructorArgTypes = constructorArgTypes;
    }

    /**
     * 创建一个新的代理对象
     * 使用AopProxy的默认类加载器（如果需要创建代理）,通常使用的是线程上下文类加载器
     */
    @Override
    public Object getProxy() {
        return getProxy(null);
    }

    /**
     * 使用指定的类加载器创建一个新的代理对象
     */
    @Override
    public Object getProxy(@Nullable ClassLoader classLoader) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("Creating CGLIB proxy: target source is " + this.advised.getTargetSource());
//		}

        try {
            Class<?> rootClass = this.advised.getTargetClass();
            Assert.state(rootClass != null, "目标类必须可用于创建CGLIB代理");

            Class<?> proxySuperClass = rootClass;
            //判断这个类是否是cglib的代理类
            if (ClassUtils.isCglibProxyClass(rootClass)) {
                //如果是的将获取这个带类的父类，也就是被代理的类
                proxySuperClass = rootClass.getSuperclass();
                //获取代理类的实现的接口，添加到需要实现的接口列表中
                Class<?>[] additionalInterfaces = rootClass.getInterfaces();
                for (Class<?> additionalInterface : additionalInterfaces) {
                    this.advised.addInterface(additionalInterface);
                }
            }

            // 验证类，根据需要编写日志消息
            validateClassIfNecessary(proxySuperClass, classLoader);

            //配置cglib增强
            Enhancer enhancer = createEnhancer();
            if (classLoader != null) {
                enhancer.setClassLoader(classLoader);
                if (classLoader instanceof SmartClassLoader &&
                        //判断proxySuperClass是否可以重载
                        ((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
                    // 关闭CGLib缓存，否则总是生成同一个类
                    enhancer.setUseCache(false);
                }
            }
            //设置产生的代理对象的父类
            enhancer.setSuperclass(proxySuperClass);
            //设置cglib需要实现的接口
            enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
            //覆盖默认命名策略
            enhancer.setNamingPolicy(SummerNamingPolicy.INSTANCE);
            //设置用此生成器创建字节码的策略
            enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));
            //获取回调列表
            Callback[] callbacks = getCallbacks(rootClass);
            //获取所有回调的类型
            Class<?>[] types = new Class<?>[callbacks.length];
            for (int x = 0; x < types.length; x++) {
                types[x] = callbacks[x].getClass();
            }
            //设置回调过滤器
            enhancer.setCallbackFilter(new ProxyCallbackFilter(
                    this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset));
            enhancer.setCallbackTypes(types);

            //生成代理类并创建代理实例
            return createProxyClassAndInstance(enhancer, callbacks);
        } catch (CodeGenerationException | IllegalArgumentException ex) {
            throw new AopConfigException("无法生成类[" +
                    this.advised.getTargetClass() + "]的CGLIB: 导致此问题的常见原因可能是使用了final类或不可见的类",
                    ex);
        } catch (Throwable ex) {
            throw new AopConfigException("未知的AOP异常", ex);
        }
    }

    /**
     * 生成代理类并创建代理实例
     *
     * @param enhancer  代理增强器
     * @param callbacks 回调方法列表
     */
    protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
        //在对象的构造器中调用这个连接点方法时不拦截
        enhancer.setInterceptDuringConstruction(false);
        //设置方法回调对象
        enhancer.setCallbacks(callbacks);
        return (this.constructorArgs != null && this.constructorArgTypes != null ?
                enhancer.create(this.constructorArgTypes, this.constructorArgs) :
                enhancer.create());
    }

    /**
     * 创建CGLIB增强
     */
    protected Enhancer createEnhancer() {
        return new Enhancer();
    }

    /**
     * 检查所提供的类是否已被验证，如果没有，则对其进行验证。
     */
    private void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
//		if (logger.isWarnEnabled()) {
        synchronized (validatedClasses) {
            if (!validatedClasses.containsKey(proxySuperClass)) {
                //进行验证
                doValidateClass(proxySuperClass, proxyClassLoader,
                        ClassUtils.getAllInterfacesForClassAsSet(proxySuperClass));
                //缓存结果
                validatedClasses.put(proxySuperClass, Boolean.TRUE);
            }
        }
//		}
    }

    /**
     * 检查给定Class上的final方法，以及跨ClassLoader的程序包可见的方法，并为找到的每个方法将警告写入日志。
     */
    private void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader, Set<Class<?>> ifcs) {
        if (proxySuperClass != Object.class) {
            //获取类中的所有方法
            Method[] methods = proxySuperClass.getDeclaredMethods();
            for (Method method : methods) {
                //获取类的修饰符
                int mod = method.getModifiers();
                //不是静态、private方法
                if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
                    //有final修饰
                    if (Modifier.isFinal(mod)) {
                        if (implementsInterface(method, ifcs)) {
//							logger.warn("Unable to proxy interface-implementing method [" + method + "] because " +
//									"it is marked as final: Consider using interface-based JDK proxies instead!");
                        }
//						logger.info("Final method [" + method + "] cannot get proxied via CGLIB: " +
//								"Calls to this method will NOT be routed to the target instance and " +
//								"might lead to NPEs against uninitialized fields in the proxy instance.");
                    } else if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) &&
                            proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
//						logger.info("Method [" + method + "] is package-visible across different ClassLoaders " +
//								"and cannot get proxied via CGLIB: Declare this method as public or protected " +
//								"if you need to support invocations through the proxy.");
                    }
                }
            }
            doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader, ifcs);
        }
    }

    /**
     * 获取回调列表
     * 方法中会将fixedCallbacks接在mainCallbacks之后形成新的Callbacks返回
     */
    private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
        // Parameters used for optimization choices...
        //代理是否公开
        boolean exposeProxy = this.advised.isExposeProxy();
        //配置是否冻结
        boolean isFrozen = this.advised.isFrozen();
        //目标是否是静态的
        boolean isStatic = this.advised.getTargetSource().isStatic();

        // 选择一个AOP拦截器
        Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

        // 选择目标拦截器，用于在不使用advice的时候返回的对象
        Callback targetInterceptor;
        if (exposeProxy) {
            targetInterceptor = isStatic ?
                    //TODO GYX 没有搞懂这两个类的作用
                    new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
                    new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
        } else {
            targetInterceptor = isStatic ?
                    new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
                    new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
        }

        //指定一个定向目标的调度器，用于在不使用advice的情况下，进行非advice的静态目标的调用
        Callback targetDispatcher = isStatic ?
                new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();

        Callback[] mainCallbacks = new Callback[]{
                //寻找的正常advice
                aopInterceptor,
                //如果优化，则在不考虑advice的情况下调用的目标
                targetInterceptor,
                //不用被覆盖映射的方法
                new SerializableNoOp(),
                targetDispatcher, this.advisedDispatcher,
                //equals的重写
                new EqualsInterceptor(this.advised),
                //hashCode的重写
                new HashCodeInterceptor(this.advised)
        };

        Callback[] callbacks;

        // 如果目标是静态目标，并且advice链被冻结，则可以使用该方法的固定链将AOP调用直接发送到目标，从而进行一些优化。
        if (isStatic && isFrozen) {
            //获取目标的方法
            Method[] methods = rootClass.getMethods();
            //设置固定的回调
            Callback[] fixedCallbacks = new Callback[methods.length];
            this.fixedInterceptorMap = new HashMap<>(methods.length);

            for (int x = 0; x < methods.length; x++) {
                //获取方法的拦截器链
                List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(methods[x], rootClass);
                //创建静态目标的固定拦截器链
                fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
                        chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
                this.fixedInterceptorMap.put(methods[x].toString(), x);
            }

            // 将mainCallbacks和fixedCallbacks的两个回调都复制到callbacks数组中
            callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
            System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
            System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
            this.fixedInterceptorOffset = mainCallbacks.length;
        } else {
            callbacks = mainCallbacks;
        }
        //返回回调列表
        return callbacks;
    }


    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof CglibAopProxy &&
                AopProxyUtils.equalsInProxy(this.advised, ((CglibAopProxy) other).advised)));
    }

    @Override
    public int hashCode() {
        return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
    }


    /**
     * 检查定接列表中的接口中是否有接口声明了给定方法
     */
    private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
        for (Class<?> ifc : ifcs) {
            //判断接口是否存在指定方法
            if (ClassUtils.hasMethod(ifc, method.getName(), method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理返回值 包装此返回值（如果有必要作为代理），并验证没有将null作为原语返回
     */
    @Nullable
    private static Object processReturnType(
            Object proxy, @Nullable Object target, Method method, @Nullable Object returnValue) {

        // 如果返回值就是目标本身， 并且没有继承RawTargetAccess类，将发挥目标的代理对象
        if (returnValue != null && returnValue == target &&
                !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
            returnValue = proxy;
        }
        //获取方法的返回类型
        Class<?> returnType = method.getReturnType();
        //如果没有返回只，并且返回类型不是void，并且返回类型是基数数据类型
        if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new AopInvocationException(
                    "advice的返回值为空与的原始返回类型不匹配: " + method);
        }
        return returnValue;
    }


    /**
     * 可替换CGLIB的NoOp接口
     */
    public static class SerializableNoOp implements NoOp, Serializable {
    }


    /**
     * 方法拦截器用于没有advice链的静态目标。 这个回调将直接传递回目标。 在需要暴露代理并且无法确定该方法会不会返回此值时使用。
     */
    private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

        @Nullable
        private final Object target;

        public StaticUnadvisedInterceptor(@Nullable Object target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object retVal = methodProxy.invoke(this.target, args);
            return processReturnType(proxy, this.target, method, retVal);
        }
    }


    /**
     * 方法拦截器用于代理公开时没有advice链的静态目标。
     */
    private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

        @Nullable
        private final Object target;

        public StaticUnadvisedExposedInterceptor(@Nullable Object target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            try {
                //替换掉aop上下文中的代理对象，并保存住原先的代理
                oldProxy = AopContext.setCurrentProxy(proxy);
                //执行这个方法
                Object retVal = methodProxy.invoke(this.target, args);
                //对返回值进行处理
                return processReturnType(proxy, this.target, method, retVal);
            } finally {
                //一定要把执行中的代理替换回去
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }


    /**
     * 这种拦截器用于调用动态目标，而无需创建方法调用或调用advice链
     */
    private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

        private final TargetSource targetSource;

        public DynamicUnadvisedInterceptor(TargetSource targetSource) {
            this.targetSource = targetSource;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            //直接获取目标对象
            Object target = this.targetSource.getTarget();
            try {
                //执行目标对象的对应方法
                Object retVal = methodProxy.invoke(target, args);
                //对返回值进行处理
                return processReturnType(proxy, target, method, retVal);
            } finally {
                if (target != null) {
                    //释放掉目标对象。。。很多时候都不会做任何处理
                    this.targetSource.releaseTarget(target);
                }
            }
        }
    }


    /**
     * 当代理公开时，拦截不使用advice的动态目标
     */
    private static class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

        private final TargetSource targetSource;

        public DynamicUnadvisedExposedInterceptor(TargetSource targetSource) {
            this.targetSource = targetSource;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            //获取目标对象
            Object target = this.targetSource.getTarget();
            try {
                //替换掉aop上下文中的代理对象，并保存住原先的代理
                oldProxy = AopContext.setCurrentProxy(proxy);
                //执行目标对象的对应方法
                Object retVal = methodProxy.invoke(target, args);
                return processReturnType(proxy, target, method, retVal);
            } finally {
                //一定要把执行中的代理替换回去
                AopContext.setCurrentProxy(oldProxy);
                if (target != null) {
                    //释放掉目标对象。。。很多时候都不会做任何处理
                    this.targetSource.releaseTarget(target);
                }
            }
        }
    }


    /**
     * 静态目标的分派器
     * 分派器比拦截器快得多，只要可以确定某个方法绝对不返回“ this”，就会使用此方法
     */
    private static class StaticDispatcher implements Dispatcher, Serializable {

        @Nullable
        private Object target;

        public StaticDispatcher(@Nullable Object target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object loadObject() {
            return this.target;
        }
    }


    /**
     * 使用分派器，处理在Advised类上声明的方法
     */
    private static class AdvisedDispatcher implements Dispatcher, Serializable {

        private final AdvisedSupport advised;

        public AdvisedDispatcher(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object loadObject() throws Exception {
            return this.advised;
        }
    }


    /**
     * 用于equals方法的分派器
     * 确保方法调用始终由此类处理
     */
    private static class EqualsInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public EqualsInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
            Object other = args[0];
            //判断传入的参数，与代理是否是同一个对象
            if (proxy == other) {
                return true;
            }
            if (other instanceof Factory) {
                //获取equal的回调
                Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
                if (!(callback instanceof EqualsInterceptor)) {
                    return false;
                }
                //获取equals分配器中的代理配置
                AdvisedSupport otherAdvised = ((EqualsInterceptor) callback).advised;
                //使用equals的代理方法判断两者配置是否相同
                return AopProxyUtils.equalsInProxy(this.advised, otherAdvised);
            } else {
                return false;
            }
        }
    }


    /**
     * hashCode方法的分派器
     */
    private static class HashCodeInterceptor implements MethodInterceptor, Serializable {

        private final AdvisedSupport advised;

        public HashCodeInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
            return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
        }
    }


    /**
     * 拦截器专门用于冻结的静态代理上的advice方法。
     */
    private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {

        /**
         * advice拦截链
         */
        private final List<Object> adviceChain;

        /**
         * 目标对象
         */
        @Nullable
        private final Object target;

        /**
         * 目标对象的类型
         */
        @Nullable
        private final Class<?> targetClass;

        public FixedChainStaticTargetInterceptor(
                List<Object> adviceChain, @Nullable Object target, @Nullable Class<?> targetClass) {

            this.adviceChain = adviceChain;
            this.target = target;
            this.targetClass = targetClass;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            //获取方法调用器
            MethodInvocation invocation = new CglibMethodInvocation(proxy, this.target, method, args,
                    this.targetClass, this.adviceChain, methodProxy);
            //执行方法
            Object retVal = invocation.proceed();
            //处理返回值
            retVal = processReturnType(proxy, this.target, method, retVal);
            return retVal;
        }
    }


    /**
     * 当目标是动态的或未冻结代理时使用AOP回调
     */
    private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

        /**
         * 代理配置
         */
        private final AdvisedSupport advised;

        public DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        @Nullable
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object oldProxy = null;
            boolean setProxyContext = false;
            Object target = null;
            //获取目标源
            TargetSource targetSource = this.advised.getTargetSource();
            try {
                // 判断代理是否暴露在上下文中
                if (this.advised.exposeProxy) {
                    //替换代理对象
                    oldProxy = AopContext.setCurrentProxy(proxy);
                    //标识代理上下文已经被修改
                    setProxyContext = true;
                }
                // 如果更换了，则要尽可能晚些以最小化我们“拥有”目标的时间
                target = targetSource.getTarget();
                Class<?> targetClass = (target != null ? target.getClass() : null);
                //获取方法拦截器链
                List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
                Object retVal;
                // 检查我们是否只有一个拦截器：也就是说，没有真正的advice，而只是对目标的反射调用
                if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
                    // 我们可以跳过创建MethodInvocation的方法：直接调用目标即可
                    // 最终的调用者必须是InvokerInterceptor，因此我们知道它只会对目标执行反射操作，并且不执行热交换或者代理。
                    Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                    retVal = methodProxy.invoke(target, argsToUse);
                }
                else {
                    // 创建方法调用器
                    retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
                }
                //处理返回值
                retVal = processReturnType(proxy, target, method, retVal);
                return retVal;
            }
            finally {
                //最后如果需要释放目标，或者将原代理放回上下文中
                if (target != null && !targetSource.isStatic()) {
                    targetSource.releaseTarget(target);
                }
                if (setProxyContext) {
                    AopContext.setCurrentProxy(oldProxy);
                }
            }
        }

        @Override
        public boolean equals(Object other) {
            return (this == other ||
                    (other instanceof DynamicAdvisedInterceptor &&
                            this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
        }

        @Override
        public int hashCode() {
            return this.advised.hashCode();
        }
    }


    /**
     * AOP代理使用的AOP Alliance MethodInvocation的实现
     */
    private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

        /**
         * 方法代理
         */
        private final MethodProxy methodProxy;

        /**
         * 方法是否是public
         */
        private final boolean publicMethod;

        public CglibMethodInvocation(Object proxy, @Nullable Object target, Method method,
                                     Object[] arguments, @Nullable Class<?> targetClass,
                                     List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {

            super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
            this.methodProxy = methodProxy;
            //判断方法的修饰符是不是Public
            this.publicMethod = Modifier.isPublic(method.getModifiers());
        }

        /**
         * 直接调用连接点方法
         */
        @Override
        protected Object invokeJoinpoint() throws Throwable {
            if (this.publicMethod) {
                //如果是个public的方法，通过方法代理执行方法
                return this.methodProxy.invoke(this.target, this.arguments);
            }
            else {
                //如果不是public的方法，则通过反射调用给定目标
                return super.invokeJoinpoint();
            }
        }
    }


    /**
     * 使用CallbackFilter将回调分配给方法
     */
    private static class ProxyCallbackFilter implements CallbackFilter {

        /**
         * AOP代理配置管理器
         */
        private final AdvisedSupport advised;

        /**
         * 固定回调，方法与回调的映射关系
         */
        private final Map<String, Integer> fixedInterceptorMap;

        /**
         * 固定拦截器偏移量
         */
        private final int fixedInterceptorOffset;

        /**
         * 初始化代理回调过滤器
         *
         * @param advised                AOP代理配置管理器
         * @param fixedInterceptorMap    方法与回调的映射关系
         * @param fixedInterceptorOffset 固定拦截器偏移量
         */
        public ProxyCallbackFilter(
                AdvisedSupport advised, Map<String, Integer> fixedInterceptorMap, int fixedInterceptorOffset) {

            this.advised = advised;
            this.fixedInterceptorMap = fixedInterceptorMap;
            this.fixedInterceptorOffset = fixedInterceptorOffset;
        }

        /**
         * 返回我们需要的回调索引
         * 每个代理的回调都由一组常规的固定回调组成，再由一组特定的使用于固定的advice链的静态目标上的方法专用的回调组成
         * 对于暴露的代理:
         * 公开代理需要在方法/链调用之前和之后执行代码。这意味着我们必须使用DynamicAdvisedInterceptor，因为所有其他拦截器都可以不使用try/catch
         * <p>
         * 对于Object.finalize():不使用此方法的替代
         * <p>
         * 对于equals:EqualsInterceptor会将equals()的调用重定向到这个代理的重写的equals()上
         * <p>
         * 对于advised接口上声明的方法:会使用AdvisedDispatcher将回调直接分配给目标
         * <p>
         * 对于advice的方法:
         * 如果目标是静态的并且advice链被冻结，则使用特定于该方法的FixedChainStaticTargetInterceptor来调用advice链
         * 否则，将使用DyanmicAdvisedInterceptor
         * <p>
         * 对于非advice的方法:
         * 在可以确定该方法将不会返回this或当getExposeProxy()返回false的情况下，将使用Dispatcher
         * 对于静态目标，使用StaticDispatcher
         * 对于动态目标，使用DynamicUnadvisedInterceptor
         */
        @Override
        public int accept(Method method) {
            //处理Object.finalize()方法
            if (AopUtils.isFinalizeMethod(method)) {
//				logger.debug("Found finalize() method - using NO_OVERRIDE");
                return NO_OVERRIDE;
            }
            //处理在advice接口上声明的方法
            if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
                    method.getDeclaringClass().isAssignableFrom(Advised.class)) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("Method is declared on Advised interface: " + method);
//				}
                return DISPATCH_ADVISED;
            }
            //处理equals方法
            if (AopUtils.isEqualsMethod(method)) {
//				logger.debug("Found 'equals' method: " + method);
                return INVOKE_EQUALS;
            }
            // 处理hashCode方法
            if (AopUtils.isHashCodeMethod(method)) {
//				logger.debug("Found 'hashCode' method: " + method);
                return INVOKE_HASHCODE;
            }
            //获取目标类型
            Class<?> targetClass = this.advised.getTargetClass();
            //获取目标方法的拦截链
            List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            //判断有没有advice
            boolean haveAdvice = !chain.isEmpty();
            //判断代理是否公开
            boolean exposeProxy = this.advised.isExposeProxy();
            //目标是否静态
            boolean isStatic = this.advised.getTargetSource().isStatic();
            //代理配置是否冻结
            boolean isFrozen = this.advised.isFrozen();
            if (haveAdvice || !isFrozen) {
                if (exposeProxy) {
                    //处理暴露的代理
//					if (logger.isDebugEnabled()) {
//						logger.debug("Must expose proxy on advised method: " + method);
//					}
                    return AOP_PROXY;
                }
                String key = method.toString();
                // Check to see if we have fixed interceptor to serve this method.
                // Else use the AOP_PROXY.
                //处理静态的advice方法
                if (isStatic && isFrozen && this.fixedInterceptorMap.containsKey(key)) {
//					if (logger.isDebugEnabled()) {
//						logger.debug("Method has advice and optimizations are enabled: " + method);
//					}
                    // We know that we are optimizing so we can use the FixedStaticChainInterceptors.
                    int index = this.fixedInterceptorMap.get(key);
                    return (index + this.fixedInterceptorOffset);
                }
                //处理动态的advice方法
                else {
//					if (logger.isDebugEnabled()) {
//						logger.debug("Unable to apply any optimizations to advised method: " + method);
//					}
                    return AOP_PROXY;
                }
            } else {
                //处理公开的动态的非advice方法
                if (exposeProxy || !isStatic) {
                    return INVOKE_TARGET;
                }
                Class<?> returnType = method.getReturnType();
                if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
//					if (logger.isDebugEnabled()) {
//						logger.debug("Method return type is assignable from target type and " +
//								"may therefore return 'this' - using INVOKE_TARGET: " + method);
//                }
                    return INVOKE_TARGET;
                } else {
//					if (logger.isDebugEnabled()) {
//						logger.debug("Method return type ensures 'this' cannot be returned - " +
//								"using DISPATCH_TARGET: " + method);
//					}
                    return DISPATCH_TARGET;
                }
            }
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ProxyCallbackFilter)) {
                return false;
            }
            ProxyCallbackFilter otherCallbackFilter = (ProxyCallbackFilter) other;
            AdvisedSupport otherAdvised = otherCallbackFilter.advised;
            if (this.advised.isFrozen() != otherAdvised.isFrozen()) {
                return false;
            }
            if (this.advised.isExposeProxy() != otherAdvised.isExposeProxy()) {
                return false;
            }
            if (this.advised.getTargetSource().isStatic() != otherAdvised.getTargetSource().isStatic()) {
                return false;
            }
            if (!AopProxyUtils.equalsProxiedInterfaces(this.advised, otherAdvised)) {
                return false;
            }
            // Advice instance identity is unimportant to the proxy class:
            // All that matters is type and ordering.
            Advisor[] thisAdvisors = this.advised.getAdvisors();
            Advisor[] thatAdvisors = otherAdvised.getAdvisors();
            if (thisAdvisors.length != thatAdvisors.length) {
                return false;
            }
            for (int i = 0; i < thisAdvisors.length; i++) {
                Advisor thisAdvisor = thisAdvisors[i];
                Advisor thatAdvisor = thatAdvisors[i];
                if (!equalsAdviceClasses(thisAdvisor, thatAdvisor)) {
                    return false;
                }
                if (!equalsPointcuts(thisAdvisor, thatAdvisor)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * AdviceClasses的equals方法
         */
        private boolean equalsAdviceClasses(Advisor a, Advisor b) {
            return (a.getAdvice().getClass() == b.getAdvice().getClass());
        }

        /**
         * Pointcuts的equals方法
         */
        private boolean equalsPointcuts(Advisor a, Advisor b) {
            return (!(a instanceof PointcutAdvisor) ||
                    (b instanceof PointcutAdvisor &&
                            ObjectUtils.nullSafeEquals(((PointcutAdvisor) a).getPointcut(), ((PointcutAdvisor) b).getPointcut())));
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            Advisor[] advisors = this.advised.getAdvisors();
            for (Advisor advisor : advisors) {
                Advice advice = advisor.getAdvice();
                hashCode = 13 * hashCode + advice.getClass().hashCode();
            }
            hashCode = 13 * hashCode + (this.advised.isFrozen() ? 1 : 0);
            hashCode = 13 * hashCode + (this.advised.isExposeProxy() ? 1 : 0);
            hashCode = 13 * hashCode + (this.advised.isOptimize() ? 1 : 0);
            hashCode = 13 * hashCode + (this.advised.isOpaque() ? 1 : 0);
            return hashCode;
        }
    }


    /**
     * 在类生成时将应用程序的ClassLoader公开为线程上下文的ClassLoader
     */
    private static class ClassLoaderAwareUndeclaredThrowableStrategy extends UndeclaredThrowableStrategy {

        @Nullable
        private final ClassLoader classLoader;

        public ClassLoaderAwareUndeclaredThrowableStrategy(@Nullable ClassLoader classLoader) {
            super(UndeclaredThrowableException.class);
            this.classLoader = classLoader;
        }

        @Override
        public byte[] generate(ClassGenerator cg) throws Exception {
            if (this.classLoader == null) {
                return super.generate(cg);
            }

            Thread currentThread = Thread.currentThread();
            ClassLoader threadContextClassLoader;
            try {
                threadContextClassLoader = currentThread.getContextClassLoader();
            }
            catch (Throwable ex) {
                // Cannot access thread context ClassLoader - falling back...
                return super.generate(cg);
            }

            boolean overrideClassLoader = !this.classLoader.equals(threadContextClassLoader);
            if (overrideClassLoader) {
                currentThread.setContextClassLoader(this.classLoader);
            }
            try {
                return super.generate(cg);
            }
            finally {
                if (overrideClassLoader) {
                    // Reset original thread context ClassLoader.
                    currentThread.setContextClassLoader(threadContextClassLoader);
                }
            }
        }
    }

}