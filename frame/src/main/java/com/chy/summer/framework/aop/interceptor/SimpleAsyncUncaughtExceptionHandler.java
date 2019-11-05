package com.chy.summer.framework.aop.interceptor;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 默认的AsyncUncaughtExceptionHandler，它只记录异常。
 */
@Slf4j
public class SimpleAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		if (log.isErrorEnabled()) {
			log.error(String.format("调用异步方法'%s'时发生意外错误", method), ex);
		}
	}

}