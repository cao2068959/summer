package com.chy.summer.framework.exception;

/**
 * 在引用当前正在创建的bean时引发异常
 * 通常在构造函数自动装配与当前构造的bean匹配时发生
 */
public class BeanCurrentlyInCreationException extends BeanCreationException {

	/**
	 * 创建一个新的BeanCurrentlyInCreationException，其默认错误消息为循环引用。
	 * @param beanName 请求的bean的名称
	 */
	public BeanCurrentlyInCreationException(String beanName) {
		super(beanName,"当前正在创建请求的bean：是否存在不可解析的循环引用？");
	}

	/**
	 * 创建一个新的BeanCurrentlyInCreationException
	 * @param beanName 请求的bean的名称
	 * @param msg 详细信息
	 */
	public BeanCurrentlyInCreationException(String beanName, String msg) {
		super(beanName, msg);
	}

}