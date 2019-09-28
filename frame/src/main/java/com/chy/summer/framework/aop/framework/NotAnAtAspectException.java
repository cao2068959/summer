package com.chy.summer.framework.aop.framework;

/**
 * AopConfigException的扩展
 * 当试图对不是AspectJ注释aspect的类执行advisor生成操作时抛出。
 */
public class NotAnAtAspectException extends AopConfigException {

	private Class<?> nonAspectClass;


	/**
	 * 为给定的类创建一个新的NotAnAtAspectException
	 * @param nonAspectClass 出错的类
	 */
	public NotAnAtAspectException(Class<?> nonAspectClass) {
		super(nonAspectClass.getName() + "不是一个@AspectJ切面");
		this.nonAspectClass = nonAspectClass;
	}

	/**
	 * 返回出错的类
	 */
	public Class<?> getNonAspectClass() {
		return this.nonAspectClass;
	}

}