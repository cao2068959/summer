package com.chy.summer.framework.aop.framework.adapter;

/**
 * 尝试使用不受支持的Advisor或Advice类型时引发的异常。
 */
public class UnknownAdviceTypeException extends IllegalArgumentException {

	/**
	 * 为给定的建议对象创建一个新的UnknownAdviceTypeException
	 * 创建一条消息文本，说明该对象既不是Advice的子接口也不是Advisor的子接口
	 */
	public UnknownAdviceTypeException(Object advice) {
		super("Advice对象[" + advice + "] 既不是Advice的子接口也不是Advisor的子接口");
	}

	/**
	 * 使用给定的消息创建一个新的UnknownAdviceTypeException
	 * @param message the message text
	 */
	public UnknownAdviceTypeException(String message) {
		super(message);
	}

}