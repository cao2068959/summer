package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aspectj.AspectJProxyUtils;
import com.chy.summer.framework.aop.aspectj.SimpleAspectInstanceFactory;
import com.chy.summer.framework.aop.framework.ProxyCreatorSupport;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import org.aspectj.lang.reflect.PerClauseKind;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AspectJProxyFactory extends ProxyCreatorSupport {

	/** 缓存单例的aspect实现 */
	private static final Map<Class<?>, Object> aspectCache = new ConcurrentHashMap<>();

	private final AspectJAdvisorFactory aspectFactory = new ReflectiveAspectJAdvisorFactory();


	/**
	 * 创建一个新的AspectJProxyFactory.
	 */
	public AspectJProxyFactory() {
	}

	/**
	 * 创建一个新的AspectJProxyFactory.
	 * 代理给定目标实现的所有接口
	 * @param target 需要代理的目标对象
	 */
	public AspectJProxyFactory(Object target) {
		Assert.notNull(target, "目标对象不可为空");
		setInterfaces(ClassUtils.getAllInterfaces(target));
		setTarget(target);
	}

	/**
	 * 创建一个新的AspectJProxyFactory
	 * 没有目标，只有接口,必须添加拦截器
	 */
	public AspectJProxyFactory(Class<?>... interfaces) {
		setInterfaces(interfaces);
	}


	/**
	 * 将提供的切面实例添加到链中
	 * 提供的切面实例的类型必须是单例切面
	 * 在使用这种方法时，真正的单例生命周期将不再起作用——调用者需要自己维护管理以这种方式添加的切面的生命周期。
	 * @param aspectInstance AspectJ Aspect实例
	 */
	public void addAspect(Object aspectInstance) {
		Class<?> aspectClass = aspectInstance.getClass();
		String aspectName = aspectClass.getName();
		AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
		if (am.getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON) {
			throw new IllegalArgumentException(
					"[" + aspectClass.getName() + "]切面类不是一个单例的切面");
		}
		addAdvisorsFromAspectInstanceFactory(
				new SingletonMetadataAwareAspectInstanceFactory(aspectInstance, aspectName));
	}

	/**
	 * 将提供的切面类型添加到advice链的末端。
	 * @param aspectClass 切面类
	 */
	public void addAspect(Class<?> aspectClass) {
		String aspectName = aspectClass.getName();
		//创建切面元数据
		AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
		//创建切面实例工厂
		MetadataAwareAspectInstanceFactory instanceFactory = createAspectInstanceFactory(am, aspectClass, aspectName);
		//Aspect实例工厂添加Advisor
		addAdvisorsFromAspectInstanceFactory(instanceFactory);
	}


	/**
	 * 将提供的MetadataAwareAspectInstanceFactory中的所有Advisor添加到当前链中
	 * 有需要的话会暴露特殊用途的Advisor
	 */
	private void addAdvisorsFromAspectInstanceFactory(MetadataAwareAspectInstanceFactory instanceFactory) {
		//创建advisor
		List<Advisor> advisors = this.aspectFactory.getAdvisors(instanceFactory);
		//获取目标类型
		Class<?> targetClass = getTargetClass();
		Assert.state(targetClass != null, "无法处理的目标类别");
		//在advisor列表中找到使用于目标类型的advisor
		advisors = AopUtils.findAdvisorsThatCanApply(advisors, targetClass);
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(advisors);
		//对advisor进行排序
		AnnotationAwareOrderComparator.sort(advisors);
		//加入到advisor列表中
		addAdvisors(advisors);
	}

	/**
	 * 为提供的切面类型创建一个AspectMetadata实例
	 */
	private AspectMetadata createAspectMetadata(Class<?> aspectClass, String aspectName) {
		AspectMetadata am = new AspectMetadata(aspectClass, aspectName);
		if (!am.getAjType().isAspect()) {
			throw new IllegalArgumentException("[" + aspectClass.getName() + "]类不是有效的切面类型");
		}
		return am;
	}

	/**
	 * 为提供的方面类型创建MetadataAwareAspectInstanceFactory
	 * 如果方面类型没有per clause，则返回SingletonMetadataAwareAspectInstanceFactory，否则返回PrototypeAspectInstanceFactory
	 */
	private MetadataAwareAspectInstanceFactory createAspectInstanceFactory(
			AspectMetadata am, Class<?> aspectClass, String aspectName) {

		MetadataAwareAspectInstanceFactory instanceFactory;
		if (am.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
			//创建一个单例的切面实例
			Object instance = getSingletonAspectInstance(aspectClass);
			instanceFactory = new SingletonMetadataAwareAspectInstanceFactory(instance, aspectName);
		}
		else {
			// 独立的切面实例创建工厂
			instanceFactory = new SimpleMetadataAwareAspectInstanceFactory(aspectClass, aspectName);
		}
		return instanceFactory;
	}

	/**
	 * 获取提供的切面类型的切面方面实例
	 * 如果缓存中没有，则会创建一个实例
	 */
	private Object getSingletonAspectInstance(Class<?> aspectClass) {
		//缓存中获取
		Object instance = aspectCache.get(aspectClass);
		if (instance == null) {
			synchronized (aspectCache) {
				//锁定缓存之后，最后一次尝试获取
				instance = aspectCache.get(aspectClass);
				if (instance != null) {
					return instance;
				}
				instance = new SimpleAspectInstanceFactory(aspectClass).getAspectInstance();
				aspectCache.put(aspectClass, instance);
			}
		}
		return instance;
	}


	/**
	 * 根据此工厂中的设置创建一个新的代理
	 * 可以反复调用。 如果我们添加或删除了interfaces，结果会有所不同。 可以添加和删除拦截器
	 * 使用默认的类加载器：通常是线程上下文类加载器
	 */
	public <T> T getProxy() {
		return (T) createAopProxy().getProxy();
	}

	/**
	 * 根据此工厂中的设置创建一个新的代理
	 * 可以反复调用。 如果我们添加或删除了interfaces，结果会有所不同。 可以添加和删除拦截器
	 * 使用给定的加载器
	 * @param classLoader 类加载器
	 */
	public <T> T getProxy(ClassLoader classLoader) {
		return (T) createAopProxy().getProxy(classLoader);
	}

}