package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.core.objenesis.SummerObjenesis;
import com.chy.summer.framework.util.ReflectionUtils;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

import java.lang.reflect.Constructor;

/**
 * CglibAopProxy的基于对象的扩展，以创建代理实例，而无需调用类的构造函数。
 */
class ObjenesisCglibAopProxy extends CglibAopProxy {

//	private static final Log logger = LogFactory.getLog(ObjenesisCglibAopProxy.class);

	private static final SummerObjenesis objenesis = new SummerObjenesis();


	/**
	 * 为给定的AOP配置创建一个新的ObjenesisCglibAopProxy
	 */
	public ObjenesisCglibAopProxy(AdvisedSupport config) {
		super(config);
	}

	/**
	 * 生成代理类并创建代理实例
	 * @param enhancer  代理增强器
	 * @param callbacks 回调方法列表
	 * @return
	 */
	@Override
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		Class<?> proxyClass = enhancer.createClass();
		Object proxyInstance = null;

		if (objenesis.isWorthTrying()) {
			try {
				proxyInstance = objenesis.newInstance(proxyClass, enhancer.getUseCache());
			}
			catch (Throwable ex) {
//				logger.debug("Unable to instantiate proxy using Objenesis, " +
//						"falling back to regular proxy construction", ex);
			}
		}

		if (proxyInstance == null) {
			// Regular instantiation via default constructor...
			try {
				Constructor<?> ctor = (this.constructorArgs != null ?
						proxyClass.getDeclaredConstructor(this.constructorArgTypes) :
						proxyClass.getDeclaredConstructor());
				ReflectionUtils.makeAccessible(ctor);
				proxyInstance = (this.constructorArgs != null ?
						ctor.newInstance(this.constructorArgs) : ctor.newInstance());
			}
			catch (Throwable ex) {
				throw new AopConfigException("无法使用Objenesis进行实例化代理，并且通过默认构造函数的常规代理实例化也失败", ex);
			}
		}

		((Factory) proxyInstance).setCallbacks(callbacks);
		return proxyInstance;
	}

}