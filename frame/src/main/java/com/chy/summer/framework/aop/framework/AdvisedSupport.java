package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.*;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.support.DefaultIntroductionAdvisor;
import com.chy.summer.framework.aop.support.DefaultPointcutAdvisor;
import com.chy.summer.framework.aop.target.EmptyTargetSource;
import com.chy.summer.framework.aop.target.SingletonTargetSource;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.CollectionUtils;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP代理配置管理器的基类
 * 这些类本身不是AOP代理，此类的子类通常是工厂类，可以直接从中获取AOP代理实例
 *
 * 主要功能有：
 * 1.配置当前代理的Adivsiors
 * 2.配置当前代理的目标对象
 * 3.配置当前代理的接口
 * 4.提供getInterceptorsAndDynamicInterceptionAdvice方法用来获取对应代理方法对应有效的拦截器链
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

	/**
	 * 没有目标时的规范目标源，行为由advisor提供。
	 */
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;


	/**
     * 可直接访问以提高效率
     */
	TargetSource targetSource = EMPTY_TARGET_SOURCE;

	/**
     * Advisors是否已针对特定目标类进行过滤
     */
	private boolean preFiltered = false;

	/**
     * advisor链的工厂
     */
	AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();

	/**
     * 缓存方法的键和advisor链的映射关系
     */
	private transient Map<MethodCacheKey, List<Object>> methodCache;

	/**
	 * 代理要实现的接口
     * 在List中以保持注册顺序，根据指定的接口顺序创建JDK代理。
	 */
	private List<Class<?>> interfaces = new ArrayList<>();

	/**
	 * advisor的名单列表
     * 如果添加了advice，则将其包装在advisor中，然后再添加到此列表中
	 */
	private List<Advisor> advisors = new LinkedList<>();

	/**
	 * 数组根据对advisor列表的更改而更新，易于内部操作
	 */
	private Advisor[] advisorArray = new Advisor[0];


	/**
	 * JavaBean的无参数构造函数
	 */
	public AdvisedSupport() {
		this.methodCache = new ConcurrentHashMap<>(32);
	}

	/**
	 * 使用给定的参数创建一个AdvisedSupport实例
	 * @param interfaces 代理接口
	 */
	public AdvisedSupport(Class<?>... interfaces) {
		this();
		//设置要代理的接口
		setInterfaces(interfaces);
	}


	/**
	 * 将给定的对象设置为目标。
     * 将为该对象创建一个SingletonTargetSource。
	 */
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}

    /**
     * 设置目标源
     */
	@Override
	public void setTargetSource(@Nullable TargetSource targetSource) {
		this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
	}

    /**
     * 获取目标源
     */
	@Override
	public TargetSource getTargetSource() {
		return this.targetSource;
	}

	/**
	 * 设置目标类型，如果没有设置将使用EmptyTargetSource的规范类型
	 */
	public void setTargetClass(Class<?> targetClass) {
		this.targetSource = EmptyTargetSource.forClass(targetClass);
	}

    /**
     * 获取目标类型
     */
	@Override
	public Class<?> getTargetClass() {
		return this.targetSource.getTargetClass();
	}

    /**
     * 设置此代理配置是否已预先过滤
     * 默认值为“ false”。
     * 如果已经对advisor进行了预过滤，则将其设置为“ true”，那么在为代理调用、构建advisor链实例时可以跳过ClassFilter检查。
     */
	@Override
	public void setPreFiltered(boolean preFiltered) {
		this.preFiltered = preFiltered;
	}

    /**
     * 返回此代理配置是否已预先过滤
     */
	@Override
	public boolean isPreFiltered() {
		return this.preFiltered;
	}

	/**
	 * 设置advisor链工厂
	 * 默认是DefaultAdvisorChainFactory
	 */
	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		Assert.notNull(advisorChainFactory, "AdvisorChainFactory不可为空");
		this.advisorChainFactory = advisorChainFactory;
	}

	/**
	 * 获取advisor链工厂，没有设置将返回null
	 */
	public AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}


	/**
	 * 设置要代理的接口
	 */
	public void setInterfaces(Class<?>... interfaces) {
		Assert.notNull(interfaces, "Interfaces不可为空");
		//清空代理需要实现的接口
		this.interfaces.clear();
		//将新的接口填充进来
		for (Class<?> ifc : interfaces) {
			addInterface(ifc);
		}
	}

	/**
	 * 添加一个新的代理接口
	 */
	public void addInterface(Class<?> intf) {
		Assert.notNull(intf, "Interface不可为空");
		if (!intf.isInterface()) {
			throw new IllegalArgumentException("[" + intf.getName() + "]不是一个interface");
		}
		//判断接口列表中是否存在这个接口
		if (!this.interfaces.contains(intf)) {
			this.interfaces.add(intf);
			//清除方法和advisor链的映射关系
			adviceChanged();
		}
	}

	/**
	 * 删除代理接口
     * 如果未代理给定接口，则不执行任何操作
	 */
	public boolean removeInterface(Class<?> intf) {
		return this.interfaces.remove(intf);
	}

    /**
     * 返回由AOP代理的接口
     */
	@Override
	public Class<?>[] getProxiedInterfaces() {
		return this.interfaces.toArray(new Class<?>[this.interfaces.size()]);
	}

    /**
     * 判断是否代理了给定的接口
     */
	@Override
	public boolean isInterfaceProxied(Class<?> intf) {
		for (Class<?> proxyIntf : this.interfaces) {
			if (intf.isAssignableFrom(proxyIntf)) {
				return true;
			}
		}
		return false;
	}

    /**
     * 获取此代理的Advisor。
     */
	@Override
	public final Advisor[] getAdvisors() {
		return this.advisorArray;
	}

    /**
     * 在Advisor链的末尾添加Advisor。
     * Advisor可以是IntroductionAdvisor
     */
	@Override
	public void addAdvisor(Advisor advisor) {
		int pos = this.advisors.size();
		addAdvisor(pos, advisor);
	}

    /**
     * 在链中的指定位置添加Advisor
     */
	@Override
	public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
		if (advisor instanceof IntroductionAdvisor) {
			validateIntroductionAdvisor((IntroductionAdvisor) advisor);
		}
		addAdvisorInternal(pos, advisor);
	}

    /**
     * 删除给定的advisor
     */
	@Override
	public boolean removeAdvisor(Advisor advisor) {
		int index = indexOf(advisor);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

    /**
     * 删除给定位置上的advisor
     */
	@Override
	public void removeAdvisor(int index) throws AopConfigException {
		if (isFrozen()) {
			throw new AopConfigException("无法删除advisor：配置已冻结");
		}
		if (index < 0 || index > this.advisors.size() - 1) {
			throw new AopConfigException("advisor索引 " + index + " 超出范围：此配置只有" + this.advisors.size() + " 个advisor");
		}

		//获取索引位置上的advisor
		Advisor advisor = this.advisors.get(index);
		if (advisor instanceof IntroductionAdvisor) {
		    //对IntroductionAdvisor类型的做特殊处理
			IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
			// 同时移除对应的 introduction接口
			for (int j = 0; j < ia.getInterfaces().length; j++) {
				removeInterface(ia.getInterfaces()[j]);
			}
		}
        //删除advisor
		this.advisors.remove(index);
		//更新advisorArray
		updateAdvisorArray();
		//清除方法的缓存
		adviceChanged();
	}

    /**
     * 获取指定advisor的索引
     */
	@Override
	public int indexOf(Advisor advisor) {
		Assert.notNull(advisor, "Advisor不可为空");
		return this.advisors.indexOf(advisor);
	}

    /**
     * 替换给定的Advisor
     * 注意：如果Advisor是IntroductionAdvisor 且替换项实现了不同的接口，则将需要重新获取代理，否则将不支持旧接口，也将不会实现新接口。
     * @param a 需要更换Advisor
     * @param b 将替换为的Advisor
     */
	@Override
	public boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
		Assert.notNull(a, "Advisor a 不可为空");
		Assert.notNull(b, "Advisor b 不可为空");
		//查询a是否存在
		int index = indexOf(a);
		if (index == -1) {
			return false;
		}
		//删除a
		removeAdvisor(index);
		//在a原先的地方插入b
		addAdvisor(index, b);
		return true;
	}

	/**
	 * 将所有给定的advisor添加到此代理配置。
	 */
	public void addAdvisors(Advisor... advisors) {
		addAdvisors(Arrays.asList(advisors));
	}

	/**
	 * 将所有给定的advisor添加到此代理配置。
	 */
	public void addAdvisors(Collection<Advisor> advisors) {
		if (isFrozen()) {
			throw new AopConfigException("无法添加顾问程序：配置已冻结");
		}
		if (!CollectionUtils.isEmpty(advisors)) {
			for (Advisor advisor : advisors) {
				if (advisor instanceof IntroductionAdvisor) {
				    //验证是否是Introduction Advisor，并将接口添加到列表中
					validateIntroductionAdvisor((IntroductionAdvisor) advisor);
				}
				Assert.notNull(advisor, "Advisor 不可为空");
				//将advisor添加到列表中
				this.advisors.add(advisor);
			}
            //更新advisorArray
			updateAdvisorArray();
            //清除方法的缓存
			adviceChanged();
		}
	}

    /**
     * 验证是否是Introduction Advisor，并将接口添加到列表中
     */
	private void validateIntroductionAdvisor(IntroductionAdvisor advisor) {
		advisor.validateInterfaces();
		//如果advisor通过了验证，我们可以进行更改
		Class<?>[] ifcs = advisor.getInterfaces();
		for (Class<?> ifc : ifcs) {
			addInterface(ifc);
		}
	}

    /**
     * 在指定位置添加Advisor
     */
	private void addAdvisorInternal(int pos, Advisor advisor) throws AopConfigException {
		Assert.notNull(advisor, "Advisor不可为空");
		if (isFrozen()) {
			throw new AopConfigException("无法添加顾问程序：配置已冻结");
		}
		if (pos > this.advisors.size()) {
			throw new IllegalArgumentException(
					"指定位置 " + pos + " 超过advisors列表长度 " + this.advisors.size());
		}
        //将advisor添加到列表中的指定位置
		this.advisors.add(pos, advisor);
        //更新advisorArray
		updateAdvisorArray();
        //清除方法的缓存
		adviceChanged();
	}

	/**
	 * 更新advisorArray
	 */
	protected final void updateAdvisorArray() {
		this.advisorArray = this.advisors.toArray(new Advisor[this.advisors.size()]);
	}

	/**
	 * 允许不受控制地访问Advisor的列表。
	 */
	protected final List<Advisor> getAdvisorsInternal() {
		return this.advisors;
	}

    /**
     * 将给定的AOP Alliance的advice添加到advice（拦截器）链的末尾。
     * 将被包装在DefaultPointcutAdvisor中，并以这种包装形式从getAdvisors()方法中返回。
     *
     * 请注意，给出的advice将适用于代理上的所有调用，甚至适用于toString()方法！较窄的方法集需要使用适当的advice实现或指定适当的切入点。
     */
	@Override
	public void addAdvice(Advice advice) throws AopConfigException {
		int pos = this.advisors.size();
		addAdvice(pos, advice);
	}

    /**
     * 将给定的AOP Alliance的advice添加到advice（拦截器）链的指定位置
     */
	@Override
	public void addAdvice(int pos, Advice advice) throws AopConfigException {
		Assert.notNull(advice, "Advice不可为空");
		if (advice instanceof IntroductionInfo) {
			addAdvisor(pos, new DefaultIntroductionAdvisor(advice, (IntroductionInfo) advice));
		}
		else if (advice instanceof DynamicIntroductionAdvice) {
			throw new AopConfigException("DynamicIntroductionAdvice只能作为IntroductionAdvisor的一部分添加");
		}
		else {
			addAdvisor(pos, new DefaultPointcutAdvisor(advice));
		}
	}

    /**
     * 删除包含给定advice的advisor
     */
	@Override
	public boolean removeAdvice(Advice advice) throws AopConfigException {
		int index = indexOf(advice);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

    /**
     * 返回给定的AOP Alliance的advice的索引（从0开始），如果查询到指定的advice，则返回-1。
     */
	@Override
	public int indexOf(Advice advice) {
		Assert.notNull(advice, "Advice不可为空");
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 给定的advice是否包含在此代理配置中的某个advisor中
	 */
	public boolean adviceIncluded(@Nullable Advice advice) {
		if (advice != null) {
		    //逐一判断
			for (Advisor advisor : this.advisors) {
				if (advisor.getAdvice() == advice) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 统计指定advice类型的个数
	 */
	public int countAdvicesOfType(@Nullable Class<?> adviceClass) {
		int count = 0;
		if (adviceClass != null) {
			for (Advisor advisor : this.advisors) {
				if (adviceClass.isInstance(advisor.getAdvice())) {
					count++;
				}
			}
		}
		return count;
	}


	/**
	 * MethodInterceptor根据此配置，获取给定方法的MethodInterceptor对象列表。
	 */
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, @Nullable Class<?> targetClass) {
		MethodCacheKey cacheKey = new MethodCacheKey(method);
		//从缓存中查找
		List<Object> cached = this.methodCache.get(cacheKey);
		if (cached == null) {
		    //没有找到的话，在从列表中查找
			cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
					this, method, targetClass);
			//加入缓存中
			this.methodCache.put(cacheKey, cached);
		}
		return cached;
	}

	/**
	 * advice更改时清空方法的映射缓存
	 */
	protected void adviceChanged() {
		this.methodCache.clear();
	}

	/**
	 * 使用无参构造方法创建的新实例上调用此方法，以便使用给定对象创建配置的独立副本。
	 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyConfigurationFrom(other, other.targetSource, new ArrayList<>(other.advisors));
	}

	/**
	 * 从给定的AdvisedSupport对象复制AOP配置，但允许替换新的目标源和给定的拦截器链。
	 * @param other AdvisedSupport对象，用于从中获取代理配置
	 * @param targetSource 新的目标源
	 * @param advisors advisor链
	 */
	protected void copyConfigurationFrom(AdvisedSupport other, TargetSource targetSource, List<Advisor> advisors) {
		copyFrom(other);
		//复制目标源
		this.targetSource = targetSource;
		//复制advisor链工厂
		this.advisorChainFactory = other.advisorChainFactory;
		//复制接口列表
		this.interfaces = new ArrayList<>(other.interfaces);
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor) {
                //验证是否是Introduction Advisor，并将接口添加到列表中
				validateIntroductionAdvisor((IntroductionAdvisor) advisor);
			}
			Assert.notNull(advisor, "Advisor不可为空");
            //将advisor添加到列表中的指定位置
			this.advisors.add(advisor);
		}
        //更新advisorArray
		updateAdvisorArray();
        //清除方法的缓存
		adviceChanged();
	}

	/**
	 * 仅构建此AdvisedSupport的配置副本，以替换目标源
	 */
	AdvisedSupport getConfigurationOnlyCopy() {
		AdvisedSupport copy = new AdvisedSupport();
		//复制目标源
		copy.copyFrom(this);
		//复制给定目标类的EmptyTargetSource
		copy.targetSource = EmptyTargetSource.forClass(getTargetClass(), getTargetSource().isStatic());
		//复制advisor链工厂
		copy.advisorChainFactory = this.advisorChainFactory;
		//复制接口列表
		copy.interfaces = this.interfaces;
		//复制advisor链
		copy.advisors = this.advisors;
        //更新advisorArray
		copy.updateAdvisorArray();
		return copy;
	}


    /**
     * 实例化支持
     */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		this.methodCache = new ConcurrentHashMap<>(32);
	}


	@Override
	public String toProxyConfigString() {
		return toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": ").append(this.interfaces.size()).append(" interfaces ");
		sb.append(ClassUtils.classNamesToString(this.interfaces)).append("; ");
		sb.append(this.advisors.size()).append(" advisors ");
		sb.append(this.advisors).append("; ");
		sb.append("targetSource [").append(this.targetSource).append("]; ");
		sb.append(super.toString());
		return sb.toString();
	}


	/**
	 * 方法简单的包装器 用于缓存方法时的键，重写equals和hashCode使得更加有效
	 */
	private static final class MethodCacheKey implements Comparable<MethodCacheKey> {

        /**
         * 持有的方法
         */
        private final Method method;
        /**
         * 方法的hash值
         */
		private final int hashCode;

		public MethodCacheKey(Method method) {
			this.method = method;
			this.hashCode = method.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			return (this == other || (other instanceof MethodCacheKey &&
					this.method == ((MethodCacheKey) other).method));
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public String toString() {
			return this.method.toString();
		}

        /**
         * 比较方法
         */
		@Override
		public int compareTo(MethodCacheKey other) {
			int result = this.method.getName().compareTo(other.method.getName());
			if (result == 0) {
				result = this.method.toString().compareTo(other.method.toString());
			}
			return result;
		}
	}

}