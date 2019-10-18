package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aspectj.AspectJExpressionPointcut;
import com.chy.summer.framework.aop.aspectj.AspectJPrecedenceInformation;
import com.chy.summer.framework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import com.chy.summer.framework.aop.support.DynamicMethodMatcherPointcut;
import com.chy.summer.framework.aop.support.Pointcuts;
import com.sun.istack.internal.Nullable;
import org.aspectj.lang.reflect.PerClauseKind;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * AspectJPointcutAdvisor的内部实现
 * 请注意，每个目标方法都将有一个此advisor实例
 */
class InstantiationModelAwarePointcutAdvisorImpl
		implements InstantiationModelAwarePointcutAdvisor, AspectJPrecedenceInformation, Serializable {

	private static Advice EMPTY_ADVICE = new Advice() {};


	private final AspectJExpressionPointcut declaredPointcut;

	private final Class<?> declaringClass;

	private final String methodName;

	private final Class<?>[] parameterTypes;

	private transient Method aspectJAdviceMethod;

	private final AspectJAdvisorFactory aspectJAdvisorFactory;

	private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;

	private final int declarationOrder;

	private final String aspectName;

	private final Pointcut pointcut;

	private final boolean lazy;

	@Nullable
	private Advice instantiatedAdvice;

	@Nullable
	private Boolean isBeforeAdvice;

	@Nullable
	private Boolean isAfterAdvice;


	public InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut,
			Method aspectJAdviceMethod, AspectJAdvisorFactory aspectJAdvisorFactory,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {

		this.declaredPointcut = declaredPointcut;
		this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
		this.methodName = aspectJAdviceMethod.getName();
		this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
		this.aspectInstanceFactory = aspectInstanceFactory;
		this.declarationOrder = declarationOrder;
		this.aspectName = aspectName;
		//懒加载的处理
		if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			//切入点的静态部分是一个惰性类型
			Pointcut preInstantiationPointcut = Pointcuts.union(
					aspectInstanceFactory.getAspectMetadata().getPerClausePointcut(), this.declaredPointcut);
			//目标实例化模型切入点
			this.pointcut = new PerTargetInstantiationModelPointcut(
					this.declaredPointcut, preInstantiationPointcut, aspectInstanceFactory);
			this.lazy = true;
		}
		else {
			//单例
			this.pointcut = this.declaredPointcut;
			this.lazy = false;
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
	}


	/**
	 * 要使用AOP的切入点。切入点的实际行为将根据通知的状态而改变
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	@Override
	public boolean isPerInstance() {
		return (getAspectMetadata().getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON);
	}

	/**
	 * 为这个advisor返回AspectJ AspectMetadata。
	 */
	public AspectMetadata getAspectMetadata() {
		return this.aspectInstanceFactory.getAspectMetadata();
	}

	/**
	 * 获取这切面的通知，可以是拦截器，事前通知，抛出通知等
	 */
	@Override
	public synchronized Advice getAdvice() {
		if (this.instantiatedAdvice == null) {
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
		return this.instantiatedAdvice;
	}

	/**
	 * 判断这个advisor是否需要延迟初始化advice
	 */
	@Override
	public boolean isLazy() {
		return this.lazy;
	}

	/**
	 * 返回此advisor是否已实例化其advice。
	 */
	@Override
	public synchronized boolean isAdviceInstantiated() {
		return (this.instantiatedAdvice != null);
	}

	/**
	 * 实例化Advice
	 * @param pointcut 切入点
	 */
	private Advice instantiateAdvice(AspectJExpressionPointcut pointcut) {
		Advice advice = this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pointcut,
				this.aspectInstanceFactory, this.declarationOrder, this.aspectName);
		return (advice != null ? advice : EMPTY_ADVICE);
	}

	/**
	 * 获取aspectInstanceFactory
	 */
	public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
		return this.aspectInstanceFactory;
	}

	/**
	 * 获取声明的切入点
	 */
	public AspectJExpressionPointcut getDeclaredPointcut() {
		return this.declaredPointcut;
	}

	/**
	 * 获取此对象的顺序值
	 */
	@Override
	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}

	/**
	 * 获取切面bean的名称
	 */
	@Override
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 获取切面内部通知的顺序优先度
	 */
	@Override
	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	/**
	 * 判断是否为前置通知
	 */
	@Override
	public boolean isBeforeAdvice() {
		if (this.isBeforeAdvice == null) {
			determineAdviceType();
		}
		return this.isBeforeAdvice;
	}

	/**
	 * 判断是否为后置通知
	 */
	@Override
	public boolean isAfterAdvice() {
		if (this.isAfterAdvice == null) {
			determineAdviceType();
		}
		return this.isAfterAdvice;
	}

	/**
	 * 不强制创建advice，确定参数类型
	 */
	private void determineAdviceType() {
		AbstractAspectJAdvisorFactory.AspectJAnnotation<?> aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(this.aspectJAdviceMethod);
		if (aspectJAnnotation == null) {
			this.isBeforeAdvice = false;
			this.isAfterAdvice = false;
		}
		else {
			switch (aspectJAnnotation.getAnnotationType()) {
				case AtAfter:
				case AtAfterReturning:
				case AtAfterThrowing:
					this.isAfterAdvice = true;
					this.isBeforeAdvice = false;
					break;
				case AtAround:
				case AtPointcut:
					this.isAfterAdvice = false;
					this.isBeforeAdvice = false;
					break;
				case AtBefore:
					this.isAfterAdvice = false;
					this.isBeforeAdvice = true;
			}
		}
	}


	@Override
	public String toString() {
		return "InstantiationModelAwarePointcutAdvisor: 表达式 [" + getDeclaredPointcut().getExpression() +
			"]; advice方法[" + this.aspectJAdviceMethod + "]; 子句类型=" +
			this.aspectInstanceFactory.getAspectMetadata().getAjType().getPerClause().getKind();

	}

	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		try {
			this.aspectJAdviceMethod = this.declaringClass.getMethod(this.methodName, this.parameterTypes);
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalStateException("找不到反序列化的advice方法", ex);
		}
	}


	/**
	 * 在通知实例化时改变其行为的切入点实现
	 * 注意，这是一个动态切入点
	 */
	private class PerTargetInstantiationModelPointcut extends DynamicMethodMatcherPointcut {

		private final AspectJExpressionPointcut declaredPointcut;

		private final Pointcut preInstantiationPointcut;

		@Nullable
		private LazySingletonAspectInstanceFactoryDecorator aspectInstanceFactory;

		private PerTargetInstantiationModelPointcut(AspectJExpressionPointcut declaredPointcut,
				Pointcut preInstantiationPointcut, MetadataAwareAspectInstanceFactory aspectInstanceFactory) {

			this.declaredPointcut = declaredPointcut;
			this.preInstantiationPointcut = preInstantiationPointcut;
			if (aspectInstanceFactory instanceof LazySingletonAspectInstanceFactoryDecorator) {
				this.aspectInstanceFactory = (LazySingletonAspectInstanceFactoryDecorator) aspectInstanceFactory;
			}
		}

		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass) {
			//我们要么在声明的切入点上实例化并匹配，要么在任意一个切入点上未实例化匹配
			return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass)) ||
					this.preInstantiationPointcut.getMethodMatcher().matches(method, targetClass);
		}

		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
			//这只能在声明的切入点上匹配
			return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass));
		}

		private boolean isAspectMaterialized() {
			return (this.aspectInstanceFactory == null || this.aspectInstanceFactory.isMaterialized());
		}
	}

}