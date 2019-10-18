
package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.exception.BaseRuntimeException;

/**
 * 如果注释配置不正确，则由AnnotationUtils和复合注释抛出。
 */
public class AnnotationConfigurationException extends BaseRuntimeException {

	public AnnotationConfigurationException(String message) {
		super(message);
	}

	public AnnotationConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
