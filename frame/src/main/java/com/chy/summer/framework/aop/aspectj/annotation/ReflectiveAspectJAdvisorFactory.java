package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.MethodBeforeAdvice;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aspectj.*;
import com.chy.summer.framework.aop.framework.AopConfigException;
import com.chy.summer.framework.aop.support.DefaultPointcutAdvisor;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.core.convert.converter.Converter;
import com.chy.summer.framework.core.convert.converter.ConvertingComparator;
import com.chy.summer.framework.util.ReflectionUtils;
import com.chy.summer.framework.util.StringUtils;
import com.chy.summer.framework.util.comparator.InstanceComparator;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * AspectJ类创建给定AspectJ类的advisor，并使用反射调用相应的advice方法
 */
@Slf4j
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory implements Serializable {

	/**
	 * 方法的排序器
	 */
	private static final Comparator<Method> METHOD_COMPARATOR;

	static {
        Comparator<Method> adviceKindComparator = new ConvertingComparator<>(
                //类型比较器
                new InstanceComparator<>(
                        Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class),
				//类型转换器
				(Converter<Method, Annotation>) method -> {
					AspectJAnnotation<?> annotation =
						AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
					return (annotation != null ? annotation.getAnnotation() : null);
				});
		Comparator<Method> methodNameComparator = new ConvertingComparator<>(Method::getName);
		METHOD_COMPARATOR = adviceKindComparator.thenComparing(methodNameComparator);
	}


	@Nullable
	private final BeanFactory beanFactory;


	/**
	 * 创建一个新的ReflectiveAspectJAdvisorFactory
	 */
	public ReflectiveAspectJAdvisorFactory() {
		this(null);
	}

	public ReflectiveAspectJAdvisorFactory(@Nullable BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * 为指定aspect实例上的所有带有AspectJ注释方法构建summer AOP advisor
	 * @param aspectInstanceFactory aspect实例工厂
	 * @return 该类的advisor列表
	 */
	@Override
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
		//获取aspect的元数据，在获取切面的类型
		Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
		//获取切面的名称
		String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
		//判断给定的类是否是一个有效的AspectJ切面
		validate(aspectClass);

		// 包装成MetadataAwareAspectInstanceFactory，用于懒加载
		MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
				new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

		List<Advisor> advisors = new LinkedList<>();
		//遍历所有的非@Pointcut注解的方法
		for (Method method : getAdvisorMethods(aspectClass)) {
			Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, advisors.size(), aspectName);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		// 如果他是一个目标切面，并且需要懒加载
		if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			//懒惰的实例化advisor
			Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
			advisors.add(0, instantiationAdvisor);
		}

		// 为每个带有DeclareParents注解的field创建一个DeclareParentsAdvisor
		for (Field field : aspectClass.getDeclaredFields()) {
			Advisor advisor = getDeclareParentsAdvisor(field);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}

	/**
	 * 获取指定类的Advisor方法
	 */
	private List<Method> getAdvisorMethods(Class<?> aspectClass) {
		final List<Method> methods = new LinkedList<>();
		//匹配“aspectClass”这个类中的所有方法，并且找到不带有Pointcut注解的方法，存入列表中
		ReflectionUtils.doWithMethods(aspectClass, method -> {
			// 排除切入点
			if (AnnotationUtils.getAnnotation(method, Pointcut.class) == null) {
				methods.add(method);
			}
		});
		//使用排序器对方法进行排序
		Collections.sort(methods, METHOD_COMPARATOR);
		return methods;
	}

	/**
	 * 为introduction领域创建一个DeclareParentsAdvisor
	 */
	@Nullable
	private Advisor getDeclareParentsAdvisor(Field introductionField) {
		DeclareParents declareParents = introductionField.getAnnotation(DeclareParents.class);
		if (declareParents == null) {
			// 不是introduction领域
			return null;
		}

		if (DeclareParents.class == declareParents.defaultImpl()) {
			throw new IllegalStateException("'defaultImpl'必须在DeclareParents上设置属性");
		}

		return new DeclareParentsAdvisor(
				introductionField.getType(), declareParents.value(), declareParents.defaultImpl());
	}


	/**
	 * 为给定的AspectJ advice方法构建一个summer AOP advisor
	 */
	@Override
	@Nullable
	public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrderInAspect, String aspectName) {
		//检测切面是否有效
		validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());
		//定位切入点，获取表达式切入点
		AspectJExpressionPointcut expressionPointcut = getPointcut(
				candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
		if (expressionPointcut == null) {
			return null;
		}
		return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
				this, aspectInstanceFactory, declarationOrderInAspect, aspectName);
	}

	/**
	 * 获取指定方法指定类的切入点表达式
	 * @param candidateAdviceMethod 指定的方法
	 * @param candidateAspectClass 方法所在的类
	 * @return
	 */
	@Nullable
	private AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod, Class<?> candidateAspectClass) {
		//查询这个方法上的注解（一般是Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class, Pointcut.class这几个）
		AspectJAnnotation<?> aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
		if (aspectJAnnotation == null) {
			//没有找到注解就不是aop的方法
			return null;
		}
		//创建一个表达式切入点
		AspectJExpressionPointcut ajexp =
				new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);
		//复制表达式
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
		if (this.beanFactory != null) {
			//赋值工厂
			ajexp.setBeanFactory(this.beanFactory);
		}
		return ajexp;
	}


	/**
	 * 为给定的AspectJ Advice方法构建summer AOP Advice.
	 */
	@Override
	@Nullable
	public Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
							MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
		//获取候选切面的类型
		Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
		validate(candidateAspectClass);
		//查找方法的通知注解类型
		AspectJAnnotation<?> aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
		if (aspectJAnnotation == null) {
			//没有通知方法
			return null;
		}

		//走到这里，说明有一个AspectJ方法，检查这是一个用AspectJ注释的类
		if (!isAspect(candidateAspectClass)) {
			throw new AopConfigException("advice必须在切面类内声明: " +
					"错误方法 '" + candidateAdviceMethod + "' 在 [" +
					candidateAspectClass.getName() + "]类中");
		}

		if (log.isDebugEnabled()) {
			log.debug("找到AspectJ方法: " + candidateAdviceMethod);
		}

		AbstractAspectJAdvice springAdvice;

		switch (aspectJAnnotation.getAnnotationType()) {
			case AtBefore:
				//前置通知
				springAdvice = new AspectJMethodBeforeAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				break;
			case AtAfter:
				//后置通知
				springAdvice = new AspectJAfterAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				break;
			case AtAfterReturning:
				//方法返回通知
				springAdvice = new AspectJAfterReturningAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
				if (StringUtils.hasText(afterReturningAnnotation.returning())) {
					//设置返回通知的名称
					springAdvice.setReturningName(afterReturningAnnotation.returning());
				}
				break;
			case AtAfterThrowing:
				//异常通知
				springAdvice = new AspectJAfterThrowingAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
				if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
					//设置异常通知的名称
					springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
				}
				break;
			case AtAround:
				//环绕通知
				springAdvice = new AspectJAroundAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				break;
			case AtPointcut:
				//切入点
				if (log.isDebugEnabled()) {
					log.debug("处理切入点 '" + candidateAdviceMethod.getName() + "'");
				}
				return null;
			default:
				throw new UnsupportedOperationException(
						"方法不支持的advice类型: " + candidateAdviceMethod);
		}

		// 现在配置advice
		springAdvice.setAspectName(aspectName);
		springAdvice.setDeclarationOrder(declarationOrder);
		//设置参数
		String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAdviceMethod);
		if (argNames != null) {
			springAdvice.setArgumentNamesFromStringArray(argNames);
		}
		//计算参数的绑定
		springAdvice.calculateArgumentBindings();
		return springAdvice;
	}


	/**
	 * 实例化方面的SyntheticInstantiationAdvisor
	 */
	protected static class SyntheticInstantiationAdvisor extends DefaultPointcutAdvisor {

		public SyntheticInstantiationAdvisor(final MetadataAwareAspectInstanceFactory aif) {
			super(aif.getAspectMetadata().getPerClausePointcut(), new MethodBeforeAdvice() {
				@Override
				public void before(Method method, Object[] args, @Nullable Object target) {
					// 只需实例化切面
					aif.getAspectInstance();
				}
			});
		}
	}

}
