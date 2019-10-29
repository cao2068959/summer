package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aopalliance.intercept.Interceptor;
import com.chy.summer.framework.aop.framework.adapter.AdvisorAdapterRegistry;
import com.chy.summer.framework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import com.chy.summer.framework.aop.framework.adapter.UnknownAdviceTypeException;
import com.chy.summer.framework.aop.target.SingletonTargetSource;
import com.chy.summer.framework.beans.BeanClassLoaderAware;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.config.ListableBeanFactory;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.exception.FactoryBeanNotInitializedException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * FactoryBean实现基于BeanFactory中的bean构建AOP代理
 *
 * MethodInterceptors和advisor由当前bean工厂中的bean名称列表标识，通过“interceptorNames”属性指定
 * 列表中的最后一项可以是目标bean的名称，也可以是TargetSource的名称，但是通常最好使用“targetName”/“target”/“TargetSource”属性
 *
 * 可以在工厂级别添加全局拦截器和advisor
 * 指定的在拦截器列表中展开，列表中包含一个“xxx*”条目，将给定的前缀与bean名匹配(例如，“global*”将匹配“globalBean1”和“globalBean2”，“*”定义的所有拦截器)。
 * 如果实现了有序接口，则匹配的拦截器将根据其返回的顺序值应用。
 *
 * 给定代理接口时，将创建JDK代理；如果未提供接口，则为实际目标类创建CGLIB代理。
 * 注意，CGLIB代理仅在目标类没有最终方法时才起作用，因为将在运行时创建才动态子类
 *
 * 可以将从这个工厂获得的代理强制转换为advised，或者获得ProxyFactoryBean引用并以编程方式操作它。
 * 这不适用于现有的原型引用。然而，它将用于随后从工厂获得的原型。对侦听的更改将立即在单例上生效(包括现有的引用)。
 * 但是，要更改接口或目标，必须从工厂获取一个新实例。这意味着从工厂获得的单例实例不具有相同的对象标识。
 * 但是，它们有相同的拦截器和目标，更改任何引用都会更改所有对象。
 */
@Slf4j
public class ProxyFactoryBean extends ProxyCreatorSupport
		implements FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {

	/**
	 * 拦截器列表中值的后缀表示扩展全局变量（前缀后面的通配符）
	 */
	public static final String GLOBAL_SUFFIX = "*";

	@Nullable
	private String[] interceptorNames;

	@Nullable
	private String targetName;

	/**
	 * 是否自动检测接口
	 */
	private boolean autodetectInterfaces = true;

	/**
	 * 是否是单例
	 */
	private boolean singleton = true;

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	private boolean freezeProxy = false;

	@Nullable
	private transient ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	private transient boolean classLoaderConfigured = false;

	@Nullable
	private transient BeanFactory beanFactory;

	/**
	 * advisor链是否已初始化
	 */
	private boolean advisorChainInitialized = false;

	/**
	 * 如果是一个单例bean，则使用这个属性缓存单例实例
	 */
	@Nullable
	private Object singletonInstance;


	/**
	 * 设置要代理的接口的名称。如果没有给出接口，将使用CGLIB创建实例
	 */
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) throws ClassNotFoundException {
		setInterfaces(proxyInterfaces);
	}

	/**
	 * 设置Advice/Advisor bean名称的列表。必须始终将其设置为在bean工厂中使用此工厂bean。
	 * 被引用的bean应该是类型为Interceptor、Advisor或Advice的bean，列表中的最后一项可以是工厂中任何bean的名称。
	 * 如果它既不是Advice也不是Advisor，那么就会添加一个新的SingletonTargetSource来包装它。
	 * 如果设置了“target”或“targetSource”或“targetName”属性，则不能使用这样的目标bean，在这种情况下，“interceptorNames”数组必须只包含Advice/Advisor bean名称
	 */
	public void setInterceptorNames(String... interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	/**
	 * 设置目标bean的名称。这是在“interceptorNames”数组末尾指定目标名称的替代方法。
	 * 也可以分别通过“ target” /“ targetSource”属性直接指定目标对象或TargetSource对象
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/**
	 * 设置是否自动检测代理接口
	 * 如果没有指定默认设置是“true”。如果没有指定接口，请关闭此标志以保证正常创建目标类的CGLIB代理。
	 */
	public void setAutodetectInterfaces(boolean autodetectInterfaces) {
		this.autodetectInterfaces = autodetectInterfaces;
	}

	/**
	 * 设置singleton属性的值
	 * 管理这个工厂是否应该总是返回相同的代理实例(这意味着相同的目标)，
	 * 或者是否应该返回一个新的原型实例，这意味着如果从prototype bean定义获得目标和拦截器，它们也可能是新的实例。
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * 指定要使用的AdvisorAdapterRegistry
	 * 默认值为全局AdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	@Override
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}

	/**
	 * 设置ClassLoader以生成代理类
	 * 默认值为bean ClassLoader，即包含BeanFactory的ClassLoader用于加载所有bean类
	 * 对于特定的代理服务器，可以重写这个方法
	 */
	public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	/**
	 * 将bean的类加载器提供给bean实例回调。
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}

	/**
	 * 将拥有的工厂提供给Bean实例的回调。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		checkInterceptorNames();
	}


	/**
	 * 返回一个代理。当客户端从该工厂bean获取bean时调用
	 * 创建要由该工厂返回的AOP代理的实例。 该实例将被缓存一个单例，并在每次调用getObject（）时创建一个代理。
	 */
	@Override
	@Nullable
	public Object getObject() throws BeansException {
		initializeAdvisorChain();
		if (isSingleton()) {
			//如果是单例  进行单例的初始化
			return getSingletonInstance();
		}
		else {
			if (this.targetName == null) {
				log.warn("通常不希望将非单例代理与单例目标一起使用。 " +
						"通过设置'targetName'属性来启用原型代理。");
			}
			//如果是原型  进行原型代理的初始化，这个代理的配置是完全独立的
			return newPrototypeInstance();
		}
	}

	/**
	 * 返回代理的类型
	 */
	@Override
	public Class<?> getObjectType() {
		synchronized (this) {
			//如果单例bean已经创建，则直接返回单例实例的类型就行了
			if (this.singletonInstance != null) {
				return this.singletonInstance.getClass();
			}
		}
		//获取aop代理的接口
		Class<?>[] ifcs = getProxiedInterfaces();
		if (ifcs.length == 1) {
			//如果只代理了一个接口  就直接返回这个接口
			return ifcs[0];
		}
		else if (ifcs.length > 1) {
			//创建一个复合接口
			return createCompositeInterface(ifcs);
		}
		else if (this.targetName != null && this.beanFactory != null) {
			//通过目标名获取类型
			return this.beanFactory.getType(this.targetName);
		}
		else {
			//通过targetSource获取目标类型
			return getTargetClass();
		}
	}

	/**
	 * 判断是否是单例
	 */
	@Override
	public boolean isSingleton() {
		return this.singleton;
	}


	/**
	 * 为给定的接口创建一个复合接口，在单个类中实现给定的接口
	 * 默认实现为给定的接口构建一个JDK代理类
	 * @param interfaces 合并的接口列表
	 */
	protected Class<?> createCompositeInterface(Class<?>[] interfaces) {
		return ClassUtils.createCompositeInterface(interfaces, this.proxyClassLoader);
	}

	/**
	 * 返回该类的代理对象的单例实例，如果尚未创建，则延迟创建它
	 */
	private synchronized Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			//刷新并获取TargetSource
			this.targetSource = freshTargetSource();
			//自动代理  && 没有代理的接口 && 代理的类型是接口
			if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
				//进行代理，先获取目标类型
				Class<?> targetClass = getTargetClass();
				if (targetClass == null) {
					throw new FactoryBeanNotInitializedException("无法确定代理的目标类");
				}
				//设置要代理的接口
				setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
			//设置是否应冻结此配置
			super.setFrozen(this.freezeProxy);
			//获取单例代理实例
			this.singletonInstance = getProxy(createAopProxy());
		}
		return this.singletonInstance;
	}

	/**
	 * 创建该类创建的代理对象的新原型实例
	 * @return 一个完全独立的代理
	 */
	private synchronized Object newPrototypeInstance() {
		// 对于原型，我们需要给代理一个独立配置的实例。
		// 没有任何代理将拥有此对象配置的实例，但将拥有独立的副本。
		if (log.isTraceEnabled()) {
			log.trace("创建原型ProxyFactoryBean配置的副本: " + this);
		}

		//复制一份代理工厂
		ProxyCreatorSupport copy = new ProxyCreatorSupport(getAopProxyFactory());
		//该副本需要一个新的advisor链和一个新的TargetSource
		//刷新并获取targetSource
		TargetSource targetSource = freshTargetSource();
		//从给定的AdvisedSupport对象复制AOP配置，替换新的目标源和给定的拦截器链
		copy.copyConfigurationFrom(this, targetSource, freshAdvisorChain());
		if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
			//进行代理，先获取目标类型
			Class<?> targetClass = targetSource.getTargetClass();
			if (targetClass != null) {
				//设置要代理的接口
				copy.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
		}
		//设置是否应冻结此配置
		copy.setFrozen(this.freezeProxy);

		if (log.isTraceEnabled()) {
			log.trace("使用ProxyCreatorSupport复制: " + copy);
		}
		//返回要公开的代理对象
		return getProxy(copy.createAopProxy());
	}

	/**
	 * 返回要公开的代理对象
	 * 默认实现使用工厂bean的类加载器来调用getProxy。可以重写来实现指定自定义类加载器
	 * @param aopProxy 用来获取代理的AopProxy实例
	 */
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy(this.proxyClassLoader);
	}

	/**
	 * 检查interceptorNames列表是否包含目标名作为最后的元素
	 * 如果找到，从列表中删除最后的名称并将其设置为targetName
	 */
	private void checkInterceptorNames() {
		if (!ObjectUtils.isEmpty(this.interceptorNames)) {
			//获取列表最后的targetName
			String finalName = this.interceptorNames[this.interceptorNames.length - 1];
			if (this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
				//链中的名称可以是Advisor / Advice或target / TargetSource
				//但是我们并不知道，必须查看bean的类型
				//结尾不是通配符的形式 && 既不是Advisor也不是Advice
				if (!finalName.endsWith(GLOBAL_SUFFIX) && !isNamedBeanAnAdvisorOrAdvice(finalName)) {
					//target不是拦截器
					this.targetName = finalName;
					if (log.isDebugEnabled()) {
						log.debug("名称为 '" + finalName + "' 且包含拦截器链的Bean不是advisor类：将其视为Target或TargetSource");
					}
					String[] newNames = new String[this.interceptorNames.length - 1];
					//拷贝一份 interceptorName的数组  但是最后一个元素丢弃掉
					System.arraycopy(this.interceptorNames, 0, newNames, 0, newNames.length);
					this.interceptorNames = newNames;
				}
			}
		}
	}

	/**
	 * 查看bean工厂的元数据，判断拦截器名称列表的bean名称是Advisor还是Advice，或者是target.
	 * Advisor或者Advice，返回true
	 * target，返回false
	 * @param beanName 需要检查的bean名称
	 */
	private boolean isNamedBeanAnAdvisorOrAdvice(String beanName) {
		Assert.state(this.beanFactory != null, "没有设置BeanFactory");
		//获取指定名称bean的类型
		Class<?> namedBeanClass = this.beanFactory.getType(beanName);
		if (namedBeanClass != null) {
			//判断这个bean是不是advisor 或者 是Advice
			return (Advisor.class.isAssignableFrom(namedBeanClass) || Advice.class.isAssignableFrom(namedBeanClass));
		}
		//如果我们没法搞清楚就只能当做target来处理
		if (log.isDebugEnabled()) {
			log.debug("无法确定名称为"+beanName+"的bean的类型-假设它既不是Advisor也不是Advice");
		}
		return false;
	}

	/**
	 * 创建顾问advisor（拦截器）链。
	 * 每次添加新的原型实例时，都会刷新从BeanFactory派生的advisor
	 * 通过工厂API以编程方式添加的拦截器不受此类更改的影响。
	 */
	private synchronized void initializeAdvisorChain() throws AopConfigException, BeansException {
		//advisor链已经初始化 直接结束
		if (this.advisorChainInitialized) {
			return;
		}

		if (!ObjectUtils.isEmpty(this.interceptorNames)) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory不可用(可能由于序列化)-无法解析拦截器名称" + Arrays.asList(this.interceptorNames));
			}

			//除非我们使用属性指定了targetSource，否则全局变量不能为最后一个
			if (this.interceptorNames[this.interceptorNames.length - 1].endsWith(GLOBAL_SUFFIX) &&
					this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
				throw new AopConfigException("全局变量之后需要一个目标");
			}

			//从bean名称中实现拦截器链
			for (String name : this.interceptorNames) {
				if (log.isTraceEnabled()) {
					log.trace("配置advisor或advice '" + name + "'");
				}
				//通配符模式的判断
				if (name.endsWith(GLOBAL_SUFFIX)) {
					if (!(this.beanFactory instanceof ListableBeanFactory)) {
						throw new AopConfigException(
								"只能对ListableBeanFactory使用全局advisor或拦截器");
					}
					addGlobalAdvisor((ListableBeanFactory) this.beanFactory,
							name.substring(0, name.length() - GLOBAL_SUFFIX.length()));
				}

				else {
					// 如果程序走到这里，我们需要添加一个命名拦截器，我们必须检查它是单例还是原型
					Object advice;
					if (this.singleton || this.beanFactory.isSingleton(name)) {
						//将真正的Advisor / Advice添加到链中
						advice = this.beanFactory.getBean(name);
					}
					else {
						// 如果是原型的advice和advisor，就使用原型替换
						// 避免仅为advisor链初始化而创建不必要的原型bean。
						advice = new PrototypePlaceholderAdvisor(name);
					}
					//创建Advisor并且添加到链中
					addAdvisorOnChainCreation(advice, name);
				}
			}
		}

		this.advisorChainInitialized = true;
	}


	/**
	 * 返回独立的advisor链
	 * 每次返回一个新的原型实例时，都需要这样做，以返回原型的advisor和Advices的不同实例
	 */
	private List<Advisor> freshAdvisorChain() {
		//获取Advisor的列表
		Advisor[] advisors = getAdvisors();
		List<Advisor> freshAdvisors = new ArrayList<>(advisors.length);
		for (Advisor advisor : advisors) {
			if (advisor instanceof PrototypePlaceholderAdvisor) {
				PrototypePlaceholderAdvisor pa = (PrototypePlaceholderAdvisor) advisor;
				if (log.isDebugEnabled()) {
					log.debug("新的bean名为 '" + pa.getBeanName() + "'");
				}
				// 用从getBean()查找得到的新原型实例替换占位符
				if (this.beanFactory == null) {
					throw new IllegalStateException("BeanFactory不可用(可能由于序列化)-无法解析原型advisor '" + pa.getBeanName() + "'");
				}
				Object bean = this.beanFactory.getBean(pa.getBeanName());
				//将advice转成advisor
				Advisor refreshedAdvisor = namedBeanToAdvisor(bean);
				freshAdvisors.add(refreshedAdvisor);
			}
			else {
				freshAdvisors.add(advisor);
			}
		}
		return freshAdvisors;
	}

	/**
	 * 添加所有全局拦截器和切入点。
	 */
	private void addGlobalAdvisor(ListableBeanFactory beanFactory, String prefix) {
		String[] globalAdvisorNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Advisor.class);
		String[] globalInterceptorNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Interceptor.class);
		List<Object> beans = new ArrayList<>(globalAdvisorNames.length + globalInterceptorNames.length);
		Map<Object, String> names = new HashMap<>(beans.size());
		for (String name : globalAdvisorNames) {
			Object bean = beanFactory.getBean(name);
			beans.add(bean);
			names.put(bean, name);
		}
		for (String name : globalInterceptorNames) {
			Object bean = beanFactory.getBean(name);
			beans.add(bean);
			names.put(bean, name);
		}
		AnnotationAwareOrderComparator.sort(beans);
		for (Object bean : beans) {
			String name = names.get(bean);
			if (name.startsWith(prefix)) {
				addAdvisorOnChainCreation(bean, name);
			}
		}
	}

	/**
	 * 在创建advice链时调用
	 * 将给定的advice, advisor或object添加到拦截器列表
	 * @param next advice, advisor或者目标对象
	 * @param name 我们从中获得该对象的bean名称
	 * bean factory
	 */
	private void addAdvisorOnChainCreation(Object next, String name) {
		//对象包装成Advisor
		Advisor advisor = namedBeanToAdvisor(next);
		if (log.isTraceEnabled()) {
			log.trace("添加名为 '" + name + "'的advisor");
		}
		addAdvisor(advisor);
	}

	/**
	 * 返回创建代理时使用的TargetSource
	 * 如果在interceptorNames列表的末尾没有指定Target，TargetSource将是这个类的TargetSource成员
	 * 否则，我们将获取目标bean并在必要时将其封装在TargetSource中
	 */
	private TargetSource freshTargetSource() {
		if (this.targetName == null) {
			if (log.isTraceEnabled()) {
				log.trace("未在“ interceptorNames”中指定的Bean名称");
			}
			return this.targetSource;
		}
		else {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory不可用(可能由于序列化)-无法解析名称为'" + this.targetName + "'");
			}
			if (log.isDebugEnabled()) {
				log.debug("刷新目标名称 '" + this.targetName + "'");
			}
			//获取target
			Object target = this.beanFactory.getBean(this.targetName);
			//如果目标原本就是TargetSource类型就直接返回，如果不是就进行封装
			return (target instanceof TargetSource ? (TargetSource) target : new SingletonTargetSource(target));
		}
	}

	/**
	 * getBean()获取的对象转换为Advisor或TargetSource
	 */
	private Advisor namedBeanToAdvisor(Object next) {
		try {
			return this.advisorAdapterRegistry.wrap(next);
		}
		catch (UnknownAdviceTypeException ex) {
			// 如果不是advisor或advice，那就是配置出现了错误。
			throw new AopConfigException("未知的顾问类型" + next.getClass() +
					"; 除最后一个条目外，只能在拦截器名称链中包括Advisor或Advice类型的bean，后者也可能是target或TargetSource", ex);
		}
	}

	/**
	 * 释放并根据advice更改重新缓存单例。
	 */
	@Override
	protected void adviceChanged() {
		super.adviceChanged();
		if (this.singleton) {
			log.debug("Advice已更改； 重新缓存单例实例");
			synchronized (this) {
				this.singletonInstance = null;
			}
		}
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
	}


	/**
	 * 在拦截器链中使用，我们需要在创建代理时用原型替换bean
	 */
	private static class PrototypePlaceholderAdvisor implements Advisor, Serializable {

		private final String beanName;

		private final String message;

		public PrototypePlaceholderAdvisor(String beanName) {
			this.beanName = beanName;
			this.message = "bean名称为'" + beanName + "'的原型Advisor/Advice的占位符";
		}

		public String getBeanName() {
			return beanName;
		}

		@Override
		public Advice getAdvice() {
			throw new UnsupportedOperationException("无法调用方法: " + this.message);
		}

		@Override
		public boolean isPerInstance() {
			throw new UnsupportedOperationException("无法调用方法: " + this.message);
		}

		@Override
		public String toString() {
			return this.message;
		}
	}

}