package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.framework.AopConfigException;
import com.chy.summer.framework.aop.framework.NotAnAtAspectException;
import com.chy.summer.framework.core.ParameterNameDiscoverer;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 工厂的抽象基类，这些工厂可以使用遵从AspectJ 5注释语法的AspectJ类创建Advisor。
 * 此类处理注释解析和验证功能，它实际上不会生成Advisor，生成过程会延迟到子类当中。
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

    /**
     * 标记方法是否需要代理，如果class上有@aspect注解，并且方法名没有AJC_MAGIC则需要代理
     */
	private static final String AJC_MAGIC = "ajc$";

    /**
     * 参数名称发现器
     */
	protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();


	/**
	 * 判断是否是个切面
     * 如果这个类上有@Aspect注解，并且不是由tAspectJ创建，那么我们就认为这个类是适用于summer的aspect
	 */
	@Override
	public boolean isAspect(Class<?> clazz) {
		return (hasAspectAnnotation(clazz) && !compiledByAjc(clazz));
	}

    /**
     * 判断指定类上是否存在@Aspect注解
     */
	private boolean hasAspectAnnotation(Class<?> clazz) {
		return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null);
	}

	/**
	 * 检测为AOP不应解析的AspectJ切面的代码样式。
	 */
	private boolean compiledByAjc(Class<?> clazz) {
		// AJType在代码样式和注释样式方面尽量提供统一的样式。
        // 我们依靠AspectJ编译器的实现细节，来区分AspectJ的代码
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().startsWith(AJC_MAGIC)) {
				return true;
			}
		}
		return false;
	}

    /**
     * 判断给定的类是否是一个有效的AspectJ切面
     * @param aspectClass 需要验证是否符合AspectJ切面样式的类
     */
	@Override
	public void validate(Class<?> aspectClass) throws AopConfigException {
		// 如果这个类的父类有@Aspect注解，但却不是一个抽象类，那么一定有错误
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null &&
				!Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			throw new AopConfigException("[" + aspectClass.getName() + "] 不能继承具体的切面 [" +
					aspectClass.getSuperclass().getName() + "]， ["+aspectClass.getSuperclass().getName()+ "]应是个抽象类");
		}

		AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
		if (!ajType.isAspect()) {
		    //如果aspectClass不是一个AspectJ切面,抛出异常
			throw new NotAnAtAspectException(aspectClass);
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
			throw new AopConfigException(aspectClass.getName() + " 使用了percflow实例化模型: AOP不支持这个功能");
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
			throw new AopConfigException(aspectClass.getName() + "使用了percflowbelow实例化模型: AOP不支持这个功能");
		}
	}

	/**
	 * 查找并返回给定方法的第一个AspectJ的注解
     * 正常情况下只有一个
	 */
	@Nullable
	protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
	    //需要查询的注解
		Class<?>[] classesToLookFor = new Class<?>[] {
				Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class, Pointcut.class};
		//对每个注解逐一查询
		for (Class<?> c : classesToLookFor) {
			AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) c);
			if (foundAnnotation != null) {
			    //获取到第一个注解直接返回
				return foundAnnotation;
			}
		}
		return null;
	}

    /**
     * 获取指定注解，并封装成注解模型
     */
	@Nullable
	private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> toLookFor) {
	    //查询方法上的注解
		A result = AnnotationUtils.findAnnotation(method, toLookFor);
		if (result != null) {
		    //封装成模型
			return new AspectJAnnotation<>(result);
		}
		else {
			return null;
		}
	}


	protected enum AspectJAnnotationType {
		AtPointcut,
		AtBefore,
		AtAfter,
		AtAfterReturning,
		AtAfterThrowing,
		AtAround
	}


	/**
	 * AspectJ注释的模型类，暴露其类型枚举和切入点表达式字符串。
	 */
	protected static class AspectJAnnotation<A extends Annotation> {

        /**
         * 解析表达式时，需要查询的方法名，切入点表达式的字符串只会出现在这两个方法里
         * “@Pointcut”注解是获取value里面参数
         * 其他注解是获取pointcut里面参数
         */
        private static final String[] EXPRESSION_PROPERTIES = new String[] {"value", "pointcut"};

        /**
         * 设置每个注解对应的AspectJ注解类型
         */
		private static Map<Class<?>, AspectJAnnotationType> annotationTypes = new HashMap<>();

		static {
			annotationTypes.put(Pointcut.class,AspectJAnnotationType.AtPointcut);
			annotationTypes.put(After.class,AspectJAnnotationType.AtAfter);
			annotationTypes.put(AfterReturning.class,AspectJAnnotationType.AtAfterReturning);
			annotationTypes.put(AfterThrowing.class,AspectJAnnotationType.AtAfterThrowing);
			annotationTypes.put(Around.class,AspectJAnnotationType.AtAround);
			annotationTypes.put(Before.class,AspectJAnnotationType.AtBefore);
		}

        /**
         * 模型持有的注解
         */
		private final A annotation;

        /**
         * 注解的类型
         */
		private final AspectJAnnotationType annotationType;

        /**
         * 切入点表达式字符串
         */
		private final String pointcutExpression;

        /**
         * 参数名称，这个东西就必要的有意思了
         * 用来调整参数的位置
         * 例如下列代码：
         * @ After(value="pointcut(p1,p2)",argNames=("p2,p1"))
         * public void A(param1,param2)
         *  p2的值就会被注入到param1上，p1的值会被注入到param2上
         */
		private final String argumentNames;

        /**
         * 初始化
         */
		public AspectJAnnotation(A annotation) {
			this.annotation = annotation;
			this.annotationType = determineAnnotationType(annotation);
			try {
			    //解析注解的表达式
				this.pointcutExpression = resolveExpression(annotation);
				this.argumentNames = (String) annotation.getClass().getMethod("argNames").invoke(annotation);
			}
			catch (Exception ex) {
				throw new IllegalArgumentException(annotation + " cannot be an AspectJ annotation", ex);
			}
		}

        /**
         * 确定注释类型
         */
		private AspectJAnnotationType determineAnnotationType(A annotation) {
			for (Class<?> type : annotationTypes.keySet()) {
				if (type.isInstance(annotation)) {
					return annotationTypes.get(type);
				}
			}
			throw new IllegalStateException("无法确定注解类型: " + annotation.toString());
		}

        /**
         * 解析表达式
         */
		private String resolveExpression(A annotation) throws Exception {
			//循环需要处理的注解方法
			for (String methodName : EXPRESSION_PROPERTIES) {
				Method method;
				try {
					//获取方法
					method = annotation.getClass().getDeclaredMethod(methodName);
				}
				catch (NoSuchMethodException ex) {
					method = null;
				}
				if (method != null) {
					//执行方法获取参数值
					String candidate = (String) method.invoke(annotation);
					if (StringUtils.hasText(candidate)) {
						return candidate;
					}
				}
			}
			throw new IllegalStateException("无法解析表达式: " + annotation);
		}

		public AspectJAnnotationType getAnnotationType() {
			return this.annotationType;
		}

		public A getAnnotation() {
			return this.annotation;
		}

		public String getPointcutExpression() {
			return this.pointcutExpression;
		}

		public String getArgumentNames() {
			return this.argumentNames;
		}

		@Override
		public String toString() {
			return this.annotation.toString();
		}
	}


	/**
	 * ParameterNameDiscoverer的实现，用于分析在AspectJ注释级别指定的argNames
	 */
	private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

		/**
		 * 获取参数名
		 */
		@Override
		@Nullable
		public String[] getParameterNames(Method method) {
			if (method.getParameterCount() == 0) {
				return new String[0];
			}
			//查询方法上的AspectJ的注解
			AspectJAnnotation<?> annotation = findAspectJAnnotationOnMethod(method);
			if (annotation == null) {
				return null;
			}
			//获取字符串标记器,使用逗号做分解符
			StringTokenizer strTok = new StringTokenizer(annotation.getArgumentNames(), ",");
			if (strTok.countTokens() > 0) {
				String[] names = new String[strTok.countTokens()];
				//将所有的参数名称根据逗号分解成数组
				for (int i = 0; i < names.length; i++) {
					names[i] = strTok.nextToken();
				}
				return names;
			}
			else {
				return null;
			}
		}

		@Override
		@Nullable
		public String[] getParameterNames(Constructor<?> ctor) {
			throw new UnsupportedOperationException("AOP无法处理构造函数建议advice");
		}
	}

}