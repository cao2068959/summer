package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.util.Assert;

import java.io.Serializable;

/**
 * 用于便捷创建代理的配置的父类，以确保所有代理创建器具有一致的属性
 */
public class ProxyConfig implements Serializable {

	/**
	 * 代理目标的方式
	 */
	private boolean proxyTargetClass = false;

	/**
	 * 是否优化
	 */
	private boolean optimize = false;

	/**
	 * 是否可以转为查询代理状态
	 */
	boolean opaque = false;

	boolean exposeProxy = false;

	private boolean frozen = false;


	/**
	 * 设置是否直接代理目标类，而不是仅代理特定的接口。 默认值为“ false”。
	 * 将此设置为“ true”可强制代理目标源的公开目标类。
	 * 如果该目标类是接口，则将为给定接口创建一个JDK代理。 如果该目标类是任何其他类，则将为给定类创建CGLIB代理。
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * 获取代理目标的方式
	 */
	public boolean isProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * 设置代理是否应执行主动的优化。 代理之间“主动优化”的确切含义会有所不同，但通常会有一些权衡。 默认值为“ false”。
	 * 例如，优化通常意味着在创建代理后建议更改将不会生效。 因此，默认情况下禁用优化。
	 * 如果其他设置无法进行优化，则可以忽略优化值“ true”：例如，如果“ exposeProxy”设置为“ true”，并且与优化不兼容。
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * 是否优化
	 */
	public boolean isOptimize() {
		return this.optimize;
	}

	/**
	 * 设置是否应防止将此配置创建的代理强制转换为“查询代理状态”。
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * 是否可以转为查询代理状态
	 */
	public boolean isOpaque() {
		return this.opaque;
	}

	/**
	 * 设置代理是否应由AOP框架公开为ThreadLocal以便通过AopContext类进行检索。
	 * 如果advice对象需要自己调用另一个advice方法，会很有帮助。
	 * 默认值为“ false”
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * 代理是否应由AOP框架公开为ThreadLocal以便通过AopContext类进行检索。
	 */
	public boolean isExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * 设置是否应冻结此配置
	 * 冻结配置后，将无法更改建议
	 * 这对于优化很有帮助，调用者在转换为Advised之后不希望能够操纵配置
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * 获取配置是否冻结，并且无法进行任何advice更改
	 */
	public boolean isFrozen() {
		return this.frozen;
	}


	/**
	 * 从另一个配置对象复制配置
	 */
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "其他ProxyConfig不可为空");
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.optimize;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
		sb.append("optimize=").append(this.optimize).append("; ");
		sb.append("opaque=").append(this.opaque).append("; ");
		sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
		sb.append("frozen=").append(this.frozen);
		return sb.toString();
	}

}