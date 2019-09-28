package com.chy.summer.framework.aop.aopalliance.intercept;

import java.lang.reflect.AccessibleObject;

/**
 * 连接点通用接口
 */
public interface Joinpoint {

	/**
	 * 执行链中的下一个注入对象
	 * @return 返回执行结果
	 * @throws Throwable
	 */
	Object proceed() throws Throwable;

	/**
	 * 获取当前连接点，返回调用的目标
	 */
	Object getThis();

	/**
	 * 获取连接点，被安装了aop的对象
	 */
	AccessibleObject getStaticPart();

}