package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.beans.Aware;
import com.chy.summer.framework.beans.BeanClassLoaderAware;
import com.chy.summer.framework.core.Ordered;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.sun.istack.internal.Nullable;

import java.io.Closeable;

/**
 * 具有代理处理器通用功能的基类，
 * 尤其是ClassLoader管理和valuateProxyInterfaces算法。
 */
public class ProxyProcessorSupport extends ProxyConfig implements Ordered, BeanClassLoaderAware, AopInfrastructureBean {

	/**
	 * 它应该在所有其他处理器之后运行，以便它可以仅向现有代理添加advisor ，而不是使用双重代理。
	 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * 代理的加载器
	 */
	@Nullable
	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	/**
	 * 类加载器是否已经配置
	 */
	private boolean classLoaderConfigured = false;


	/**
	 * 设置Ordered的排序，在应用多个处理器时使用。
	 * 默认值为{Integer.MAX_VALUE，表示它是无序的。
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取排序顺序
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 设置ClassLoader以便生成代理类。
	 */
	public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	/**
	 * 获取处理器配置的代理的类加载器
	 */
	@Nullable
	protected ClassLoader getProxyClassLoader() {
		return this.proxyClassLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}


	/**
	 * 检查给定bean类上的接口，并在适当时它们会应用于ProxyFactory。
	 * 调用isConfigurationCallbackInterface（java.lang.Class <？>）和isInternalLanguageInterface（java.lang.Class <？>）筛选合理的代理接口，否则返回目标类代理。
	 */
	protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
		//获取目标bean上的所有接口
		Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
		//具有合理的代理接口
		boolean hasReasonableProxyInterface = false;
		for (Class<?> ifc : targetInterfaces) {
			//判断目标类上是否有合理的接口
			if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) &&
					ifc.getMethods().length > 0) {
				hasReasonableProxyInterface = true;
				break;
			}
		}
		if (hasReasonableProxyInterface) {
			//设置Interface,不能仅将接口设置为目标的接口。
			for (Class<?> ifc : targetInterfaces) {
				proxyFactory.addInterface(ifc);
			}
		}
		else {
			//将接口设置为目标的接口
			proxyFactory.setProxyTargetClass(true);
		}
	}

	/**
	 * 判断给定的接口是否仅仅是容器回调，如果是将不被视为合理的代理接口
	 * 如果找不到给定bean的合理代理接口，则将其完整的目标类作为代理
	 */
	protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
		//TODO GYX 这里缺少类型
//		return (InitializingBean.class == ifc || DisposableBean.class == ifc || Closeable.class == ifc ||
//				AutoCloseable.class == ifc || ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
		return false;
	}

	/**
	 * 判断给定的接口是否是其他工具的内部接口，如果是不被视为合理的代理接口
	 * 如果找不到给定bean的合理代理接口，则将其完整的目标类作为代理
	 */
	protected boolean isInternalLanguageInterface(Class<?> ifc) {
		return (ifc.getName().equals("groovy.lang.GroovyObject") ||
				ifc.getName().endsWith(".cglib.proxy.Factory") ||
				ifc.getName().endsWith(".bytebuddy.MockAccess"));
	}

}