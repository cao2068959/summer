package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.TargetClassAware;
import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.aopalliance.Advice;

/**
 * 由包含AOP代理工厂配置的类实现的接口。
 * 此配置包括拦截器和其他advice，Advisor以及代理接口。
 * 从Spring获得的任何AOP代理都可以转换为该接口，以允许对其AOP建议进行操作。
 */
public interface Advised extends TargetClassAware {

	/**
	 * 返回advice配置是否被冻结，在这种情况下，不能更改建议
	 */
	boolean isFrozen();

	/**
	 * 判断代理的是目标类还是指定接口
     * True：目标类
     * False：指定接口
	 */
	boolean isProxyTargetClass();

	/**
	 * 返回由AOP代理的接口
	 */
	Class<?>[] getProxiedInterfaces();

	/**
	 * 判断是否代理了给定的接口
	 */
	boolean isInterfaceProxied(Class<?> intf);

	/**
	 * 更改Advised对象使用的目标源
     * 仅在未冻结配置的情况下可以使用
	 */
	void setTargetSource(TargetSource targetSource);

	/**
	 * 获取Advised对象使用的目标源
	 */
	TargetSource getTargetSource();

	/**
     * 设置代理是否应由AOP框架公开为ThreadLocal以便通过AopContext类进行检索。
     * 如果advice对象需要自己调用另一个advice方法，会很有帮助。
     * 默认值为“ false”
	 */
	void setExposeProxy(boolean exposeProxy);

	/**
	 * 代理是否应由AOP框架公开为ThreadLocal以便通过AopContext类进行检索。
	 */
	boolean isExposeProxy();

	/**
	 * 设置此代理配置是否已预先过滤
     * 默认值为“ false”。
     * 如果已经对advisor进行了预过滤，则将其设置为“ true”，那么在为代理调用、构建advisor链实例时可以跳过ClassFilter检查。
	 */
	void setPreFiltered(boolean preFiltered);

	/**
	 * 返回此代理配置是否已预先过滤
	 */
	boolean isPreFiltered();

	/**
	 * 获取此代理的Advisor。
	 */
	Advisor[] getAdvisors();

	/**
	 * 在Advisor链的末尾添加Advisor。
     * Advisor可以是IntroductionAdvisor
	 */
	void addAdvisor(Advisor advisor) throws AopConfigException;

	/**
	 * 在链中的指定位置添加Advisor
	 */
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

	/**
	 * 删除给定的Advisor
	 */
	boolean removeAdvisor(Advisor advisor);

	/**
	 * 删除给定索引处的Advisor
	 */
	void removeAdvisor(int index) throws AopConfigException;

	/**
	 * 返回给定Advisor的索引（从0开始）
     * 如果没有适用于此代理的Advisor，则返回-1。
	 */
	int indexOf(Advisor advisor);

	/**
	 * 替换给定的Advisor
     * 注意：如果Advisor是IntroductionAdvisor 且替换项实现了不同的接口，则将需要重新获取代理，否则将不支持旧接口，也将不会实现新接口。
	 * @param a 需要更换Advisor
	 * @param b 将替换为的Advisor
	 */
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

	/**
     * 将给定的AOP Alliance的advice添加到advice（拦截器）链的末尾。
     * 将被包装在DefaultPointcutAdvisor中，并以这种包装形式从getAdvisors()方法中返回。
     *
     * 请注意，给出的advice将适用于代理上的所有调用，甚至适用于toString()方法！较窄的方法集需要使用适当的advice实现或指定适当的切入点。
	 */
	void addAdvice(Advice advice) throws AopConfigException;

	/**
     * 将给定的AOP Alliance的advice添加到advice（拦截器）链的指定位置
	 */
	void addAdvice(int pos, Advice advice) throws AopConfigException;

	/**
	 * 删除包含给定advice的advisor
	 */
	boolean removeAdvice(Advice advice);

	/**
	 * 返回给定的AOP Alliance的advice的索引（从0开始），如果查询到指定的advice，则返回-1。
	 */
	int indexOf(Advice advice);

	/**
	 * 代理配置的字符串描述
	 */
	String toProxyConfigString();

}