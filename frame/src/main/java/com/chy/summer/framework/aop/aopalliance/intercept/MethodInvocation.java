package com.chy.summer.framework.aop.aopalliance.intercept;

import java.lang.reflect.Method;

/**
 * 方法调用的接口，获取aop连接点的相关对象
 */
public interface MethodInvocation extends Invocation {

	/**
	 * 获取正在调用的方法
	 * {@link Joinpoint#getStaticPart()} 与这个方法类似，结果相同
	 */
	Method getMethod();
}