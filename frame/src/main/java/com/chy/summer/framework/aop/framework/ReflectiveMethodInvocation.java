package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.core.BridgeMethodResolver;
import javax.annotation.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展 ProxyMethodInvocation接口
 * 使用反射调用目标对象
 * 子类可以重写 invokeJoinpoint()方法以更改调用行为，因此可以针对特别的MethodInvocation进行实现
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {

    /**
     * 对其进行调用的代理对象
     */
	protected final Object proxy;

    /**
     * 目标类
     */
	@Nullable
	protected final Object target;

    /**
     * 目标方法
     */
	protected final Method method;

    /**
     * 目标方法的参数
     */
	protected Object[] arguments = new Object[0];

    /**
     * 目标类的类型
     */
	@Nullable
	private final Class<?> targetClass;

	/**
	 * 用户特定属性的延迟初始化映射
	 */
	@Nullable
	private Map<String, Object> userAttributes;

	/**
	 * 需要动态检查的MethodInterceptor和InterceptorAndDynamicMethodMatcher的列表
	 */
	protected final List<?> interceptorsAndDynamicMethodMatchers;

    /**
     * 当前调用的拦截器，在interceptorsAndDynamicMethodMatchers中的位置索引
     */
	private int currentInterceptorIndex = -1;


	/**
	 * 根据指定参数创建新的ReflectiveMethodInvocation
	 * @param proxy 对其进行调用的代理对象
	 * @param target 目标类
	 * @param method 目标方法
	 * @param arguments 对应方法的参数
	 * @param targetClass 目标类的类型
	 * @param interceptorsAndDynamicMethodMatchers  对应的拦截器
	 */
	protected ReflectiveMethodInvocation(
			Object proxy, @Nullable Object target, Method method, @Nullable Object[] arguments,
			@Nullable Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {

		this.proxy = proxy;
		this.target = target;
		this.targetClass = targetClass;
		this.method = BridgeMethodResolver.findBridgedMethod(method);
		this.arguments = AopProxyUtils.adaptArgumentsIfNecessary(method, arguments);
		this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
	}

    /**
     * 获取调用的代理对象
     */
	@Override
	public final Object getProxy() {
		return this.proxy;
	}

    /**
     * 获取当前连接点，返回调用的目标
     */
	@Override
	@Nullable
	public final Object getThis() {
		return this.target;
	}

    /**
     * 获取连接点，被安装了aop的对象
     */
	@Override
	public final AccessibleObject getStaticPart() {
		return this.method;
	}

	/**
	 * 获取正在调用的方法
	 */
	@Override
	public final Method getMethod() {
		return this.method;
	}

    /**
     * 获取连接点的方法参数，根据顺序转为数组
     */
	@Override
	public final Object[] getArguments() {
		return this.arguments;
	}

    /**
     * 设置要在后续调用中使用的参数
     */
	@Override
	public void setArguments(Object... arguments) {
		this.arguments = arguments;
	}

    /**
     * 执行链中的下一个注入对象
     */
	@Override
	@Nullable
	public Object proceed() throws Throwable {
		//如果执行到链条的末尾 则直接调用连接点方法 即 直接调用目标方法
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}
        //获取集合中的 MethodInterceptor
		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		//如果要动态匹配joinPoint
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			InterceptorAndDynamicMethodMatcher dm =
					(InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			//动态匹配：运行时参数是否满足匹配条件
			if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
			    //可以正确匹配方法
				return dm.interceptor.invoke(this);
			}
			else {
				//动态匹配失败时,略过当前Intercetpor,调用下一个Interceptor
				return proceed();
			}
		}
		else {
			//执行当前Intercetpor
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}

	/**
	 * 执行连接点方法
	 */
	@Nullable
	protected Object invokeJoinpoint() throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
	}


	/**
	 * 浅克隆当前对象，但深度克隆方法参数
	 */
	@Override
	public MethodInvocation invocableClone() {
		Object[] cloneArguments = this.arguments;
		if (this.arguments.length > 0) {
			// 创建一个独立的参数副本对象
			cloneArguments = new Object[this.arguments.length];
			System.arraycopy(this.arguments, 0, cloneArguments, 0, this.arguments.length);
		}
		return invocableClone(cloneArguments);
	}

	/**
	 * 克隆当前对象，并且从新指定方法参数
	 */
	@Override
	public MethodInvocation invocableClone(Object... arguments) {
		//强制初始化用户属性的map
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}

		/*
		 创建this的拷贝，将独立的参数副本传入克隆出来的ReflectiveMethodInvocation中
		 此时新的ReflectiveMethodInvocation与原先的ReflectiveMethodInvocation，arguments属性的值相同，但是所持的引用不同，
		 但是其他的引用数据类型，两个ReflectiveMethodInvocation都持有统一个引用，所以上面初始化了用户属性
		 */
		try {
			ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
			clone.arguments = arguments;
			return clone;
		}
		catch (CloneNotSupportedException ex) {
			throw new IllegalStateException(
					"[" + getClass() + "]应该是一个能够克隆类型的对象: " + ex);
		}
	}

    /**
     * 设置用户参数
     */
	@Override
	public void setUserAttribute(String key, @Nullable Object value) {
		if (value != null) {
			if (this.userAttributes == null) {
				this.userAttributes = new HashMap<>();
			}
			this.userAttributes.put(key, value);
		}
		else {
			if (this.userAttributes != null) {
				this.userAttributes.remove(key);
			}
		}
	}

    /**
     * 获取用户参数
     * @param key 属性名称
     */
	@Override
	@Nullable
	public Object getUserAttribute(String key) {
		return (this.userAttributes != null ? this.userAttributes.get(key) : null);
	}

	/**
	 * 返回与此调用关联的用户属性
	 */
	public Map<String, Object> getUserAttributes() {
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}
		return this.userAttributes;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ReflectiveMethodInvocation: ");
		sb.append(this.method).append("; ");
		if (this.target == null) {
			sb.append("target为空");
		}
		else {
			sb.append("target的类型为[").append(this.target.getClass().getName()).append(']');
		}
		return sb.toString();
	}

}