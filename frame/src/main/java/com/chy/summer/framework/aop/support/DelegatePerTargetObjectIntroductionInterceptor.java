package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.DynamicIntroductionAdvice;
import com.chy.summer.framework.aop.IntroductionInterceptor;
import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.ReflectionUtils;
import com.sun.istack.internal.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 实现IntroductionInterceptor接口。
 * 这与DelegatingIntroductionInterceptor的不同之处在于，可以使用此类的单个实例来通知多个目标对象，
 * 并且每个目标对象将具有自己的委托（而DelegatingIntroductionInterceptor共享同一个委托，因此在所有目标上都具有相同的状态）。
 *
 * preventInterface方法可用于禁止委托类实现的特定的接口，但不应将其引入拥有的AOP代理。
 *
 * 如果委托是此类的实例，则可序列化。
 */
public class DelegatePerTargetObjectIntroductionInterceptor extends IntroductionInfoSupport
		implements IntroductionInterceptor {

	/**
	 * 对象与其delegate的映射关系
	 * 使用WeakHashMap，采用弱引用，不干扰垃圾回收
	 */
	private final Map<Object, Object> delegateMap = new WeakHashMap<>();

	/**
	 * 默认实现类型
	 */
	private Class<?> defaultImplType;

	/**
	 * 接口类型
	 */
	private Class<?> interfaceType;


	public DelegatePerTargetObjectIntroductionInterceptor(Class<?> defaultImplType, Class<?> interfaceType) {
		this.defaultImplType = defaultImplType;
		this.interfaceType = interfaceType;
		//现在创建一个新的委托（但不要将其存储在Map中）。
		//有两个原因：
		//1）如果实例化委托存在问题，让其尽早失败
		//2）只填充一次接口Map
		Object delegate = createNewDelegate();
		//公开defaultImplType上的所有接口
		implementInterfacesOnObject(delegate);
		//去除公开这两个接口
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}


	/**
	 * 如果子类想要在环绕通知中执行自定义方法，则可能需要重写此子类。
	 * 但是，子类应调用此方法，该方法将处理引入的接口并转发到目标。
	 */
	@Override
	@Nullable
	public Object invoke(MethodInvocation mi) throws Throwable {
		//判断改方法是否存在于接口中
		if (isMethodOnIntroducedInterface(mi)) {
			Object delegate = getIntroductionDelegateFor(mi.getThis());

			//通过反射执行指定方法
			Object retVal = AopUtils.invokeJoinpointUsingReflection(delegate, mi.getMethod(), mi.getArguments());

			// 如果委托人返回了自己，我们要改变值，返回其代理。
			if (retVal == delegate && mi instanceof ProxyMethodInvocation) {
				retVal = ((ProxyMethodInvocation) mi).getProxy();
			}
			return retVal;
		}

		return doProceed(mi);
	}

	/**
	 * 调用执行链中的下一个注入对象
	 * 子类可以重写此方法，以拦截对目标对象的方法调用
	 */
	protected Object doProceed(MethodInvocation mi) throws Throwable {
		return mi.proceed();
	}

	/**
	 * 获取IntroductionDelegate
	 */
	private Object getIntroductionDelegateFor(Object targetObject) {
		synchronized (this.delegateMap) {
			if (this.delegateMap.containsKey(targetObject)) {
				//如果关系列表中包含了这个对象的Delegate，就直接拿出来给他
				return this.delegateMap.get(targetObject);
			}
			else {
				//如果没有包含，就新建一个，缓存住
				Object delegate = createNewDelegate();
				this.delegateMap.put(targetObject, delegate);
				return delegate;
			}
		}
	}

	/**
	 * 创建一个新的Delegate
	 */
	private Object createNewDelegate() {
		try {
			return ReflectionUtils.accessibleConstructor(this.defaultImplType).newInstance();
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException("无法在'" + this.interfaceType.getName()
					+ "'中混入(" + this.defaultImplType.getName() + ")创建默认实现: " + ex);
		}
	}

}