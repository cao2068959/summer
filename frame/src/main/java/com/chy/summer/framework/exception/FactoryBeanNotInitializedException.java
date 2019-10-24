package com.chy.summer.framework.exception;

/**
 * 如果bean尚未完全初始化（例如，因为它包含在循环引用中），则从FactoryBean调用getObject()方法引发异常。
 */
public class FactoryBeanNotInitializedException extends FatalBeanException {

	public FactoryBeanNotInitializedException() {
		super("FactoryBean还没有完全初始化");
	}

	public FactoryBeanNotInitializedException(String msg) {
		super(msg);
	}

}