package com.chy.summer.framework.aop.framework;

/**
 * 非法AOP配置参数的异常
 */
public class AopConfigException extends RuntimeException {

	public AopConfigException(String msg) {
		super(msg);
	}

	public AopConfigException(String msg, Throwable cause) {
		super(msg, cause);
	}
}