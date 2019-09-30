package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.core.SmartClassLoader;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.*;
import net.sf.cglib.transform.impl.UndeclaredThrowableStrategy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

	/** Keeps track of the Classes that we have validated for final methods */
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
	 * @param constructorArgs 构造函数参数值
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
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
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
		}
		catch (CodeGenerationException | IllegalArgumentException ex) {
			throw new AopConfigException("无法生成类[" +
					this.advised.getTargetClass() + "]的CGLIB: 导致此问题的常见原因可能是使用了final类或不可见的类",
					ex);
		}
		catch (Throwable ex) {
			// TargetSource.getTarget() failed
			throw new AopConfigException("未知的AOP异常", ex);
		}
	}

	/**
	 * 生成代理类并创建代理实例
	 * @param enhancer 代理增强器
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
	 * Creates the CGLIB {@link Enhancer}. Subclasses may wish to override this to return a custom
	 * {@link Enhancer} implementation.
	 */
	protected Enhancer createEnhancer() {
		return new Enhancer();
	}

	/**
	 * Checks to see whether the supplied {@code Class} has already been validated and
	 * validates it if not.
	 */
	private void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
		if (logger.isWarnEnabled()) {
			synchronized (validatedClasses) {
				if (!validatedClasses.containsKey(proxySuperClass)) {
					doValidateClass(proxySuperClass, proxyClassLoader,
							ClassUtils.getAllInterfacesForClassAsSet(proxySuperClass));
					validatedClasses.put(proxySuperClass, Boolean.TRUE);
				}
			}
		}
	}

	/**
	 * Checks for final methods on the given {@code Class}, as well as package-visible
	 * methods across ClassLoaders, and writes warnings to the log for each one found.
	 */
	private void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader, Set<Class<?>> ifcs) {
		if (proxySuperClass != Object.class) {
			Method[] methods = proxySuperClass.getDeclaredMethods();
			for (Method method : methods) {
				int mod = method.getModifiers();
				if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
					if (Modifier.isFinal(mod)) {
						if (implementsInterface(method, ifcs)) {
							logger.warn("Unable to proxy interface-implementing method [" + method + "] because " +
									"it is marked as final: Consider using interface-based JDK proxies instead!");
						}
						logger.info("Final method [" + method + "] cannot get proxied via CGLIB: " +
								"Calls to this method will NOT be routed to the target instance and " +
								"might lead to NPEs against uninitialized fields in the proxy instance.");
					}
					else if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) &&
							proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
						logger.info("Method [" + method + "] is package-visible across different ClassLoaders " +
								"and cannot get proxied via CGLIB: Declare this method as public or protected " +
								"if you need to support invocations through the proxy.");
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
		}
		else {
			targetInterceptor = isStatic ?
					new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
		}

		//指定一个定向目标的调度器，用于在不使用advice的情况下，进行非advice的静态目标的调用
		Callback targetDispatcher = isStatic ?
				new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();

		Callback[] mainCallbacks = new Callback[] {
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
		}
		else {
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
	 * Check whether the given method is declared on any of the given interfaces.
	 */
	private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
		for (Class<?> ifc : ifcs) {
			if (ClassUtils.hasMethod(ifc, method.getName(), method.getParameterTypes())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Process a return value. Wraps a return of {@code this} if necessary to be the
	 * {@code proxy} and also verifies that {@code null} is not returned as a primitive.
	 */
	@Nullable
	private static Object processReturnType(
			Object proxy, @Nullable Object target, Method method, @Nullable Object returnValue) {

		// Massage return value if necessary
		if (returnValue != null && returnValue == target &&
				!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
			// Special case: it returned "this". Note that we can't help
			// if the target sets a reference to itself in another returned object.
			returnValue = proxy;
		}
		Class<?> returnType = method.getReturnType();
		if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
			throw new AopInvocationException(
					"Null return value from advice does not match primitive return type for: " + method);
		}
		return returnValue;
	}


	/**
	 * Serializable replacement for CGLIB's NoOp interface.
	 * Public to allow use elsewhere in the framework.
	 */
	public static class SerializableNoOp implements NoOp, Serializable {
	}


	/**
	 * Method interceptor used for static targets with no advice chain. The call
	 * is passed directly back to the target. Used when the proxy needs to be
	 * exposed and it can't be determined that the method won't return
	 * {@code this}.
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
	 * Method interceptor used for static targets with no advice chain, when the
	 * proxy is to be exposed.
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
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(this.target, args);
				return processReturnType(proxy, this.target, method, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Interceptor used to invoke a dynamic target without creating a method
	 * invocation or evaluating an advice chain. (We know there was no advice
	 * for this method.)
	 */
	private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final TargetSource targetSource;

		public DynamicUnadvisedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Override
		@Nullable
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object target = this.targetSource.getTarget();
			try {
				Object retVal = methodProxy.invoke(target, args);
				return processReturnType(proxy, target, method, retVal);
			}
			finally {
				if (target != null) {
					this.targetSource.releaseTarget(target);
				}
			}
		}
	}


	/**
	 * Interceptor for unadvised dynamic targets when the proxy needs exposing.
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
			Object target = this.targetSource.getTarget();
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(target, args);
				return processReturnType(proxy, target, method, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
				if (target != null) {
					this.targetSource.releaseTarget(target);
				}
			}
		}
	}


	/**
	 * Dispatcher for a static target. Dispatcher is much faster than
	 * interceptor. This will be used whenever it can be determined that a
	 * method definitely does not return "this"
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
	 * Dispatcher for any methods declared on the Advised class.
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
	 * Dispatcher for the {@code equals} method.
	 * Ensures that the method call is always handled by this class.
	 */
	private static class EqualsInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public EqualsInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			Object other = args[0];
			if (proxy == other) {
				return true;
			}
			if (other instanceof Factory) {
				Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
				if (!(callback instanceof EqualsInterceptor)) {
					return false;
				}
				AdvisedSupport otherAdvised = ((EqualsInterceptor) callback).advised;
				return AopProxyUtils.equalsInProxy(this.advised, otherAdvised);
			}
			else {
				return false;
			}
		}
	}


	/**
	 * Dispatcher for the {@code hashCode} method.
	 * Ensures that the method call is always handled by this class.
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
	 * Interceptor used specifically for advised methods on a frozen, static proxy.
	 */
	private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {

		private final List<Object> adviceChain;

		@Nullable
		private final Object target;

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
			MethodInvocation invocation = new CglibMethodInvocation(proxy, this.target, method, args,
					this.targetClass, this.adviceChain, methodProxy);
			// If we get here, we need to create a MethodInvocation.
			Object retVal = invocation.proceed();
			retVal = processReturnType(proxy, this.target, method, retVal);
			return retVal;
		}
	}


	/**
	 * General purpose AOP callback. Used when the target is dynamic or when the
	 * proxy is not frozen.
	 */
	private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

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
			TargetSource targetSource = this.advised.getTargetSource();
			try {
				if (this.advised.exposeProxy) {
					// Make invocation available if necessary.
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				// Get as late as possible to minimize the time we "own" the target, in case it comes from a pool...
				target = targetSource.getTarget();
				Class<?> targetClass = (target != null ? target.getClass() : null);
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
				Object retVal;
				// Check whether we only have one InvokerInterceptor: that is,
				// no real advice, but just reflective invocation of the target.
				if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
					// We can skip creating a MethodInvocation: just invoke the target directly.
					// Note that the final invoker must be an InvokerInterceptor, so we know
					// it does nothing but a reflective operation on the target, and no hot
					// swapping or fancy proxying.
					Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					retVal = methodProxy.invoke(target, argsToUse);
				}
				else {
					// We need to create a method invocation...
					retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
				}
				retVal = processReturnType(proxy, target, method, retVal);
				return retVal;
			}
			finally {
				if (target != null && !targetSource.isStatic()) {
					targetSource.releaseTarget(target);
				}
				if (setProxyContext) {
					// Restore old proxy.
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

		/**
		 * CGLIB uses this to drive proxy creation.
		 */
		@Override
		public int hashCode() {
			return this.advised.hashCode();
		}
	}


	/**
	 * Implementation of AOP Alliance MethodInvocation used by this AOP proxy.
	 */
	private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

		private final MethodProxy methodProxy;

		private final boolean publicMethod;

		public CglibMethodInvocation(Object proxy, @Nullable Object target, Method method,
				Object[] arguments, @Nullable Class<?> targetClass,
				List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {

			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.methodProxy = methodProxy;
			this.publicMethod = Modifier.isPublic(method.getModifiers());
		}

		/**
		 * Gives a marginal performance improvement versus using reflection to
		 * invoke the target when invoking public methods.
		 */
		@Override
		protected Object invokeJoinpoint() throws Throwable {
			if (this.publicMethod) {
				return this.methodProxy.invoke(this.target, this.arguments);
			}
			else {
				return super.invokeJoinpoint();
			}
		}
	}


	/**
	 * 回调过滤器，主要用于将固定回调分配给对相应方法
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
		 * @param advised AOP代理配置管理器
		 * @param fixedInterceptorMap 方法与回调的映射关系
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
		 *
		 * 对于Object.finalize():不使用此方法的替代
		 *
		 * 对于equals:EqualsInterceptor会将equals()的调用重定向到这个代理的重写的equals()上
		 *
		 * 对于advised接口上声明的方法:会使用AdvisedDispatcher将回调直接分配给目标
		 *
		 * 对于advice的方法:
		 * 如果目标是静态的并且advice链被冻结，则使用特定于该方法的FixedChainStaticTargetInterceptor来调用advice链
		 * 否则，将使用DyanmicAdvisedInterceptor
		 *
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
			}
			else {
				//处理公开的动态的非advice方法
				if (exposeProxy || !isStatic) {
					return INVOKE_TARGET;
				}
				Class<?> returnType = method.getReturnType();
				if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
//					if (logger.isDebugEnabled()) {
//						logger.debug("Method return type is assignable from target type and " +
//								"may therefore return 'this' - using INVOKE_TARGET: " + method);
					}
					return INVOKE_TARGET;
				}
				else {
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

		private boolean equalsAdviceClasses(Advisor a, Advisor b) {
			return (a.getAdvice().getClass() == b.getAdvice().getClass());
		}

		private boolean equalsPointcuts(Advisor a, Advisor b) {
			// If only one of the advisor (but not both) is PointcutAdvisor, then it is a mismatch.
			// Takes care of the situations where an IntroductionAdvisor is used (see SPR-3959).
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
	 * CGLIB GeneratorStrategy variant which exposes the application ClassLoader
	 * as thread context ClassLoader for the time of class generation
	 * (in order for ASM to pick it up when doing common superclass resolution).
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