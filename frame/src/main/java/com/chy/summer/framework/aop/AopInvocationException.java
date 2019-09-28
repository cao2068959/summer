package com.chy.summer.framework.aop;

/**
 * 当AOP调用失败时抛出的异常
 * 主要由于配置错误或意外的运行时问题
 */
public class AopInvocationException extends RuntimeException {

	public AopInvocationException(String msg) {
		super(msg);
	}

	public AopInvocationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}