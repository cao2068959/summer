package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.util.ReflectionUtils;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

import java.lang.reflect.Constructor;

class ObjenesisCglibAopProxy extends CglibAopProxy {

//	private static final Log logger = LogFactory.getLog(ObjenesisCglibAopProxy.class);

//	private static final SpringObjenesis objenesis = new SpringObjenesis();
//
//
//	/**
//	 * Create a new ObjenesisCglibAopProxy for the given AOP configuration.
//	 * @param config the AOP configuration as AdvisedSupport object
//	 */
	public ObjenesisCglibAopProxy(AdvisedSupport config) {
		super(config);
	}
//
//
//	@Override
//	@SuppressWarnings("unchecked")
//	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
//		Class<?> proxyClass = enhancer.createClass();
//		Object proxyInstance = null;
//
//		if (objenesis.isWorthTrying()) {
//			try {
//				proxyInstance = objenesis.newInstance(proxyClass, enhancer.getUseCache());
//			}
//			catch (Throwable ex) {
////				logger.debug("Unable to instantiate proxy using Objenesis, " +
////						"falling back to regular proxy construction", ex);
//			}
//		}
//
//		if (proxyInstance == null) {
//			// Regular instantiation via default constructor...
//			try {
//				Constructor<?> ctor = (this.constructorArgs != null ?
//						proxyClass.getDeclaredConstructor(this.constructorArgTypes) :
//						proxyClass.getDeclaredConstructor());
//				ReflectionUtils.makeAccessible(ctor);
//				proxyInstance = (this.constructorArgs != null ?
//						ctor.newInstance(this.constructorArgs) : ctor.newInstance());
//			}
//			catch (Throwable ex) {
//				throw new AopConfigException("无法使用Objenesis进行实例化代理，并且通过默认构造函数的常规代理实例化也失败", ex);
//			}
//		}
//
//		((Factory) proxyInstance).setCallbacks(callbacks);
//		return proxyInstance;
//	}

}