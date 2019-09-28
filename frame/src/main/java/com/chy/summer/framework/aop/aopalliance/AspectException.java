package com.chy.summer.framework.aop.aopalliance;

public class AspectException extends RuntimeException {

	/**
	 * aop异常的构造方法
	 * @param message 错误信息
	 */
	public AspectException(String message) {
		super(message);
	}

	/**
	 * aop异常的构造方法
	 * @param message 错误信息
	 * @param cause 追溯原因
	 */
	public AspectException(String message, Throwable cause) {
		super(message, cause);
	}

}