package com.chy.summer.framework.exception;

import javax.annotation.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * 当BeanFactory试图从bean定义创建bean时遇到错误时抛出异常
 */
@SuppressWarnings("serial")
public class BeanCreationException extends FatalBeanException {

	/**
	 * bean名称
	 */
	@Nullable
	private String beanName;

	/**
	 * bean定义来自的资源的描述
	 */
	@Nullable
	private String resourceDescription;

	/**
	 * 错误相关原因
	 */
	@Nullable
	private List<Throwable> relatedCauses;


	/**
	 * 创建一个新的BeanCreationException
	 * @param msg 详细消息
	 */
	public BeanCreationException(String msg) {
		super(msg);
	}

	/**
	 * 创建一个新的BeanCreationException
	 * @param msg 详细消息
	 * @param cause 错误源
	 */
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * 创建一个新的BeanCreationException
	 * @param beanName 请求的bean的名称
	 * @param msg 详细消息
	 */
	public BeanCreationException(String beanName, String msg) {
		super("创建名称为 '" + beanName + "'的bean错误: " + msg);
		this.beanName = beanName;
	}

	/**
	 * 创建一个新的BeanCreationException
	 * @param beanName 请求的bean的名称
	 * @param msg 详细消息
	 * @param cause 错误源
	 */
	public BeanCreationException(String beanName, String msg, Throwable cause) {
		this(beanName, msg);
		//initCause()这个方法就是对异常来进行包装的，目的就是为了出了问题的时候能够追根究底
		initCause(cause);
	}

	/**
	 * 创建一个新的BeanCreationException
	 * @param resourceDescription bean定义来自的资源的描述
	 * @param beanName 请求的bean的名称
	 * @param msg 详细消息
	 */
	public BeanCreationException(@Nullable String resourceDescription, @Nullable String beanName, String msg) {
		super("创建名为'" + beanName + "'的bean时出错" +
				(resourceDescription != null ? ",资源的描述" + resourceDescription : "") + ": " + msg);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}

	/**
	 * 创建一个新的BeanCreationException
	 * @param resourceDescription bean定义来自的资源的描述
	 * @param beanName 请求的bean的名称
	 * @param msg 详细消息
	 * @param cause 错误源
	 */
	public BeanCreationException(@Nullable String resourceDescription, String beanName, String msg, Throwable cause) {
		this(resourceDescription, beanName, msg);
		//initCause()这个方法就是对异常来进行包装的，目的就是为了出了问题的时候能够追根究底
		initCause(cause);
	}


	/**
	 * 获取bean定义来自的资源的描述
	 */
	@Nullable
	public String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * 获取所请求的bean的名称
	 */
	@Nullable
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * 向此bean创建异常添加一个相关原因，它不是失败的直接原因，而是在创建相同bean实例的早期发生的。
	 * @param ex 错误相关原因
	 */
	public void addRelatedCause(Throwable ex) {
		if (this.relatedCauses == null) {
			this.relatedCauses = new LinkedList<>();
		}
		this.relatedCauses.add(ex);
	}

	/**
	 * 获取相关原因
	 */
	@Nullable
	public Throwable[] getRelatedCauses() {
		if (this.relatedCauses == null) {
			return null;
		}
		return this.relatedCauses.toArray(new Throwable[this.relatedCauses.size()]);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		if (this.relatedCauses != null) {
			for (Throwable relatedCause : this.relatedCauses) {
				sb.append("\nRelated cause: ");
				sb.append(relatedCause);
			}
		}
		return sb.toString();
	}

	@Override
	public void printStackTrace(PrintStream ps) {
		synchronized (ps) {
			super.printStackTrace(ps);
			if (this.relatedCauses != null) {
				for (Throwable relatedCause : this.relatedCauses) {
					ps.println("Related cause:");
					relatedCause.printStackTrace(ps);
				}
			}
		}
	}

	@Override
	public void printStackTrace(PrintWriter pw) {
		synchronized (pw) {
			super.printStackTrace(pw);
			if (this.relatedCauses != null) {
				for (Throwable relatedCause : this.relatedCauses) {
					pw.println("Related cause:");
					relatedCause.printStackTrace(pw);
				}
			}
		}
	}

	@Override
	public boolean contains(@Nullable Class<?> exClass) {
		if (super.contains(exClass)) {
			return true;
		}
		if (this.relatedCauses != null) {
			for (Throwable relatedCause : this.relatedCauses) {
				if (relatedCause instanceof BaseRuntimeException &&
						((BaseRuntimeException) relatedCause).contains(exClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
