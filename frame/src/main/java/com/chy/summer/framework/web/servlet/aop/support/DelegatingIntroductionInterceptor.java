package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.DynamicIntroductionAdvice;
import com.chy.summer.framework.aop.IntroductionInterceptor;
import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

/**
 * 实现IntroductionInterceptor接口。
 * 子类需要扩展此类并实现要自己引入的接口。在这种情况下，委托的是子类实例本身。
 * 或者，可以使用单独的委托来实现接口，并通过委托bean属性进行设置。
 */
public class DelegatingIntroductionInterceptor extends IntroductionInfoSupport
		implements IntroductionInterceptor {

	/**
	 * 实现接口的实例对象
	 * 如果子类实现引入的接口，则可能为“ this”。
	 */
	@Nullable
	private Object delegate;


	/**
	 * 构造一个新的DelegatingIntroductionInterceptor，
	 * 提供一个实现接口的实例对象
	 */
	public DelegatingIntroductionInterceptor(Object delegate) {
		init(delegate);
	}

	/**
	 * 构造一个新的DelegatingIntroductionInterceptor。
	 * 委托类将是子类，该子类必须实现接口。
	 */
	protected DelegatingIntroductionInterceptor() {
		init(this);
	}


	/**
	 * 这两个构造函数都使用此init方法，因为无法将“ this”引用从一个构造函数传递给另一个构造函数。
	 */
	private void init(Object delegate) {
		Assert.notNull(delegate, "Delegate不可为空");
		this.delegate = delegate;
		implementInterfacesOnObject(delegate);

		// 去除公开这两个接口
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
			//通过反射执行指定方法
			Object retVal = AopUtils.invokeJoinpointUsingReflection(this.delegate, mi.getMethod(), mi.getArguments());

			// 如果委托人返回了自己，我们要改变值，返回其代理。
			if (retVal == this.delegate && mi instanceof ProxyMethodInvocation) {
				Object proxy = ((ProxyMethodInvocation) mi).getProxy();
				if (mi.getMethod().getReturnType().isInstance(proxy)) {
					retVal = proxy;
				}
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

}
