package com.chy.summer.framework.exception;

import javax.annotation.Nullable;

/**
 * 在Bean或bean的子类包中遇到不可恢复的问题时抛出
 */
public class FatalBeanException extends BeansException {

	/**
	 * 使用指定的消息创建一个新的FatalBeanException
	 * @param msg 详细信息
	 */
	public FatalBeanException(String msg) {
		super(msg);
	}

	/**
	 * 使用指定的消息和错误源创建一个新的FatalBeanException
	 * @param msg 详细信息
	 * @param cause 错误源
	 */
	public FatalBeanException(String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}