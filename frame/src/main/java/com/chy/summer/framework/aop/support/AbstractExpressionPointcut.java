package com.chy.summer.framework.aop.support;

import javax.annotation.Nullable;

import java.io.Serializable;

/**
 * 切入点表达式的抽象类，
 * 提供调试位置和表达式属性
 */
public abstract class AbstractExpressionPointcut implements ExpressionPointcut, Serializable {

	/**
	 * 位置信息
	 */
	@Nullable
	private String location;

	/**
	 * 切点表达式
	 */
	@Nullable
	private String expression;


	/**
	 * 设置调试位置
	 */
	public void setLocation(@Nullable String location) {
		this.location = location;
	}

	/**
	 * 返回切入点表达式的位置信息
	 */
	@Nullable
	public String getLocation() {
		return this.location;
	}

	/**
	 * 设置表达式
	 * @param expression 表达式字符串
	 */
	public void setExpression(@Nullable String expression) {
		//设置表达式
		this.expression = expression;
		try {
			//处理表达式，由子类实现，达到多态效果
			onSetExpression(expression);
		}
		catch (IllegalArgumentException ex) {
			// 抛出异常的时候最好可以显示错误位置
			if (this.location != null) {
				throw new IllegalArgumentException("在 [" + this.location + "]上的切点表达式无效: " + ex);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * 在设置新的切入点表达式时调用。最好在此时解析表达式
	 * 这个实现是空的，由子类实现
	 * @param expression 切入点表达式
	 * @throws IllegalArgumentException 表达式无效的时候应当抛出异常
	 */
	protected void onSetExpression(@Nullable String expression) throws IllegalArgumentException {
	}

	/**
	 * 获取切入点的表达式
	 */
	@Override
	@Nullable
	public String getExpression() {
		return this.expression;
	}
}