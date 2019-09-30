package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.AopInvocationException;
import com.chy.summer.framework.aop.RawTargetAccess;
import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.core.DecoratingProxy;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 基于JDK动态代理的AOP框架的AopProxy实现
 *
 * 创建一个动态代理，实现AopProxy的接口。 jdk动态代理只能用于代理接口中定义的方法
 * 此类对象应从AdvisedSupport类配置的代理工厂获得
 * 此类在AOP框架内部使用，并且不需要应用程序代码直接调用
 * 如果基础（目标）类是线程安全的，则使用此类创建的代理也将是线程安全的
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

//	/** We use a static Log to avoid serialization issues */
//	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/**
	 * 代理的配置
	 */
	private final AdvisedSupport advised;

	/**
	 * 代理接口上是否重写了equals方法？
	 */
	private boolean equalsDefined;

	/**
	 * 代理接口上是否重写了hashCode方法？
	 */
	private boolean hashCodeDefined;


	/**
	 * 为给定的AOP配置构造一个新的JdkDynamicAopProxy。
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport不可为空");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("没有advisors，也没有指定TargetSource");
		}
		this.advised = config;
	}

	/**
	 * 创建一个新的代理对象
	 * 使用AopProxy的默认类加载器（如果需要创建代理）,通常使用的是线程上下文类加载器
	 */
	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 获取代理类要实现的接口,除了Advised对象中配置的,还会加上SpringProxy, Advised(opaque=false)
	 * 检查上面得到的接口中有没有定义 equals或者hashcode的接口
	 * 调用Proxy.newProxyInstance创建代理对象
	 */
	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
//		}
		//获取给定AOP配置的代理的完整接口列表
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
		//判断是重写了equals和hashCode方法
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		//创建代理对象
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}

	/**
	 * 查找指定接口上可能定义的任何一组equals或hashCode方法
	 */
	private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
		for (Class<?> proxiedInterface : proxiedInterfaces) {
			//获取接口上的方法
			Method[] methods = proxiedInterface.getDeclaredMethods();
			//逐一对方法进行判断
			for (Method method : methods) {
				//判断是否重写了equals方法
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				//判断是否重写了hashCode方法
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			}
		}
	}


	/**
	 * 执行传入代理的指定方法
	 */
	@Override
	@Nullable
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Object target = null;

		try {
			//eqauls()方法，具目标对象未实现此方法
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// 如果传入的代理没有实现equals方法，则使用默认this的equals实现
				return equals(args[0]);
			}
			//hashCode()方法，具目标对象未实现此方法
			else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// 如果传入的代理没有实现hashCode方法，则使用默认this的hashCode实现
				return hashCode();
			}
			else if (method.getDeclaringClass() == DecoratingProxy.class) {
				//如果是getDecoratedClass方法，则返回adviced代理目标的类型
				return AopProxyUtils.ultimateTargetClass(this.advised);
			}
			//Advised接口或者其父接口中定义的方法,直接反射调用,不应用通知
			else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// 调用指定方法
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;

			//判断advised是否暴露在本地线程变量当中
			if (this.advised.exposeProxy) {
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			//获得目标源的实例对象
			target = targetSource.getTarget();
			//获取实例对象的类型
			Class<?> targetClass = (target != null ? target.getClass() : null);

			//获取可以应用到此方法上的Interceptor链
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			//如果没有可以应用到此方法的通知(Interceptor)，此直接反射调用 method.invoke(target, args)
			if (chain.isEmpty()) {
				//获取方法的参数
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				//指定指定方法
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			else {
				//创建MethodInvocation
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// 通过拦截器链进入连接点
				retVal = invocation.proceed();
			}

			// 获取方法的返回类型
			Class<?> returnType = method.getReturnType();

			if (retVal != null && retVal == target &&
					returnType != Object.class && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// 如果目标在返回的对象中设置了对自身的引用,只能返回代理本身
				retVal = proxy;
			}
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"advice的返回值为空，与的原始返回类型不匹配: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				//释放掉目标对象
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				//将上下文中代理替换回去
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * 被比较的对象可以是JdkDynamicAopProxy实例本身，也可以是包装JdkDynamicAopProxy实例的动态代理。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		JdkDynamicAopProxy otherProxy;
		//比较JdkDynamicAopProxy实例
		if (other instanceof JdkDynamicAopProxy) {
			otherProxy = (JdkDynamicAopProxy) other;
		}
		//比较包装JdkDynamicAopProxy实例的动态代理
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = (JdkDynamicAopProxy) ih;
		}
		else {
			return false;
		}

		// 检查给定AdvisedSupport对象里的代理是否相等。
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * 代理使用TargetSource的hash码
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

}