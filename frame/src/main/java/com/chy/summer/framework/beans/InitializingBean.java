package com.chy.summer.framework.beans;


/**
 * bean实现的接口
 * 一旦BeanFactory设置好了bean的所有属性，bean就需要立即做出响应（调用afterPropertiesSet()方法，此方法在bean的后置处理器之前执行）
 * 例如，执行自定义初始化，或者只是检查是否设置了所有必需的属性。
 */
public interface InitializingBean {

	/**
	 * 当BeanFactory设置了所提供的所有bean属性(并满足BeanFactoryAware和applicationcontextAware)之后调用
	 * 此方法仅在设置了所有bean属性后才允许bean实例执行初始化，并在配置错误的情况下抛出异常
	 */
	void afterPropertiesSet() throws Exception;

}