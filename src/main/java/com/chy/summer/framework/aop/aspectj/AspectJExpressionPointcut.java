package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.IntroductionAwareMethodMatcher;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.aop.interceptor.ExposeInvocationInterceptor;
import com.chy.summer.framework.aop.support.AopUtils;
import org.aspectj.weaver.reflect.ReflectionWorld.ReflectionWorldException;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.AbstractExpressionPointcut;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.ConfigurableBeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
import org.aspectj.weaver.patterns.NamePattern;
import org.aspectj.weaver.reflect.ReflectionWorld;
import org.aspectj.weaver.reflect.ShadowMatchImpl;
import org.aspectj.weaver.tools.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计算切入点表达式的切入点实现。
 *
 * 切入点表达式值使用AspectJ表达式。这可以引用其他切入点，并使用组合和其他操作。
 * 最小颗粒度支持到方法执行切入点。
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut
		implements ClassFilter, IntroductionAwareMethodMatcher, BeanFactoryAware {

	private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

	static {
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
	}

    /**
     * 切入点声明范围
     */
	@Nullable
	private Class<?> pointcutDeclarationScope;

	/**
	 * 切入点参数名称
	 */
	private String[] pointcutParameterNames = new String[0];

	private Class<?>[] pointcutParameterTypes = new Class<?>[0];

	@Nullable
	private BeanFactory beanFactory;

	@Nullable
	private transient ClassLoader pointcutClassLoader;

	/**
	 * 切入点表达式
	 */
	@Nullable
	private transient PointcutExpression pointcutExpression;
	/**
	 * 通过方法获取对应的shadowMatch方法，尽量保证并发下不会出现锁竞争
	 * shadowMatch不知道怎么翻译比较好,大概的意思就是模糊匹配,或者是匹配的精确度
	 */
	private transient Map<Method, ShadowMatch> shadowMatchCache = new ConcurrentHashMap<>(32);


	/**
	 * 创建一个新的默认AspectJExpressionPointcut
	 */
	public AspectJExpressionPointcut() {
	}

	/**
	 * 使用给定的设置创建一个新的AspectJExpressionPointcut。
	 * @param declarationScope 切入点的声明范围
	 * @param paramNames 切入点的参数名称
	 * @param paramTypes 切入点的参数类型
	 */
	public AspectJExpressionPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes) {
		this.pointcutDeclarationScope = declarationScope;
		if (paramNames.length != paramTypes.length) {
			throw new IllegalStateException(
					"切入点参数名称的数目必须与切入点参数类型的数目一致");
		}
		this.pointcutParameterNames = paramNames;
		this.pointcutParameterTypes = paramTypes;
	}


	/**
	 * 设置切入点的声明范围
	 */
	public void setPointcutDeclarationScope(Class<?> pointcutDeclarationScope) {
		this.pointcutDeclarationScope = pointcutDeclarationScope;
	}

	/**
	 * 设置切入点的参数名称
	 */
	public void setParameterNames(String... names) {
		this.pointcutParameterNames = names;
	}

	/**
	 * 设置切入点的参数类型
	 */
	public void setParameterTypes(Class<?>... types) {
		this.pointcutParameterTypes = types;
	}

    /**
     * 设置bean工厂
     */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

    /**
     * 获取类过滤器
     */
	@Override
	public ClassFilter getClassFilter() {
		obtainPointcutExpression();
		return this;
	}

    /**
     * 获取方法匹配器
     */
	@Override
	public MethodMatcher getMethodMatcher() {
		obtainPointcutExpression();
		return this;
	}


	/**
	 * 检查这个切入点是否准备好匹配
	 * 创建底层aspectj切入点表达式
	 */
	private PointcutExpression obtainPointcutExpression() {
		if (getExpression() == null) {
			throw new IllegalStateException("在匹配之前必须设置属性“expression”");
		}
		if (this.pointcutExpression == null) {
			//获取切入点使用的类加载器
			this.pointcutClassLoader = determinePointcutClassLoader();
			//构建切入点表达式
			this.pointcutExpression = buildPointcutExpression(this.pointcutClassLoader);
		}
		return this.pointcutExpression;
	}

	/**
	 * 用于确定切入点使用的类加载器
	 */
	@Nullable
	private ClassLoader determinePointcutClassLoader() {
		//尝试从bean工厂中获取类加载器
		if (this.beanFactory instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) this.beanFactory).getBeanClassLoader();
		}
		//从切点声明范围中获取类加载器
		if (this.pointcutDeclarationScope != null) {
			return this.pointcutDeclarationScope.getClassLoader();
		}
		//都没有获取到的时候，使用默认的类加载器
		return ClassUtils.getDefaultClassLoader();
	}

	/**
	 * 构建底层AspectJ切入点表达式
	 */
	private PointcutExpression buildPointcutExpression(@Nullable ClassLoader classLoader) {
		//初始化切入点解析器
		PointcutParser parser = initializePointcutParser(classLoader);
		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
		//将切入点参数名与切入点参数类型一一对应，创建切入点参数列表
		for (int i = 0; i < pointcutParameters.length; i++) {
			pointcutParameters[i] = parser.createPointcutParameter(
					this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
		}
		//根据切入点表达式字符串、切入点声明范围、切入点参数解析创建切入点表达式
		return parser.parsePointcutExpression(replaceBooleanOperators(resolveExpression()),
				this.pointcutDeclarationScope, pointcutParameters);
	}

	/**
	 * 获取切入点的表达式字符串，没有设置时会抛出异常
	 */
	private String resolveExpression() {
		String expression = getExpression();
		Assert.state(expression != null, "没有设置表达式");
		return expression;
	}

	/**
	 * 初始化底层AspectJ切入点解析器
	 */
	private PointcutParser initializePointcutParser(@Nullable ClassLoader classLoader) {
		//根据指定原语并使用指定类加载器获取的切入点解析器
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
						SUPPORTED_PRIMITIVES, classLoader);
		//注册一个bean的切点表达式处理器，用来处理summer直接支持的切点表达式
		parser.registerPointcutDesignatorHandler(new BeanPointcutDesignatorHandler());
		return parser;
	}


	/**
	 * 如果在XML中指定了类型范围，则用户不能将"and"写成“&&”
	 */
	private String replaceBooleanOperators(String pcExpr) {
		String result = StringUtils.replace(pcExpr, " and ", " && ");
		result = StringUtils.replace(result, " or ", " || ");
		result = StringUtils.replace(result, " not ", " ! ");
		return result;
	}


	/**
	 * 获取底层AspectJ切入点表达式
	 */
	public PointcutExpression getPointcutExpression() {
		return obtainPointcutExpression();
	}

	/**
	 * 使用切入点表达式匹配目标类
	 */
	@Override
	public boolean matches(Class<?> targetClass) {
		//获取切入点表达式
		PointcutExpression pointcutExpression = obtainPointcutExpression();
		try {
			try {
				//检测是否是匹配的连接点类型
				return pointcutExpression.couldMatchJoinPointsInType(targetClass);
			}
			catch (ReflectionWorld.ReflectionWorldException ex) {
//				logger.debug("PointcutExpression matching rejected target class - trying fallback expression", ex);
				// 仍有可能是动态切入点，从新获取切入点表达式
				PointcutExpression fallbackExpression = getFallbackPointcutExpression(targetClass);
				if (fallbackExpression != null) {
					//重新检测匹配
					return fallbackExpression.couldMatchJoinPointsInType(targetClass);
				}
			}
		}
		catch (Throwable ex) {
//			logger.debug("PointcutExpression matching rejected target class", ex);
		}
		return false;
	}

	/**
	 * 使用给定的方法在指定的类中匹配对应方法
	 * @param method 需要匹配的方法
	 * @param targetClass 目标类
	 * @param beanHasIntroductions bean是否拥有Introduction
	 * @return 返回匹配结果
	 */
	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass, boolean beanHasIntroductions) {
		//检查这个切入点是否准备好匹配
		obtainPointcutExpression();
		//指定类中的指定方法
		Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		//获取ShadowMatch对象并缓存
		ShadowMatch shadowMatch = getShadowMatch(targetMethod, method);

		//对target， @this， @target， @annotation特殊处理
		//因为我们知道这个类在运行时永远不会有匹配的子类，所以可以优化。
		if (shadowMatch.alwaysMatches()) {
			//永远匹配,如果切入点表达式一直可以匹配此模糊匹配的任何连接点，则为true
			return true;
		}
		else if (shadowMatch.neverMatches()) {
			//永远不匹配,如果切入点表达式用于无法匹配此模糊匹配的任何连接点，则为false
			return false;
		}
		else {
			//其他的情况下
			if (beanHasIntroductions) {
				//bean如果有Introduction则直接返回true
				return true;
			}
			// 使用RuntimeTestWalker这个类解析内部规则
			RuntimeTestWalker walker = getRuntimeTestWalker(shadowMatch);
			//是否有测试子类型敏感，测试的目标实例是否有差异（细节见spr3783）
			return (!walker.testsSubtypeSensitiveVars() ||
					(targetClass != null && walker.testTargetInstanceOfResidue(targetClass)));
		}
	}

	/**
	 * 使用给定的方法在指定的类中匹配对应方法,并且bean没有Introduction
	 * @param method 需要匹配的方法
	 * @param targetClass 目标类
	 * @return 返回匹配结果
	 */
	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass) {
		return matches(method, targetClass, false);
	}

	/**
	 * 是否是动态的
	 */
	@Override
	public boolean isRuntime() {
		return obtainPointcutExpression().mayNeedDynamicTest();
	}

	/**
	 * 动态匹配
	 * @param method
	 * @param targetClass
	 * @param args
	 * @return
	 */
	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
		//检查这个切入点是否准备好匹配
		obtainPointcutExpression();
		//获取ShadowMatch对象并缓存
		ShadowMatch shadowMatch = getShadowMatch(AopUtils.getMostSpecificMethod(method, targetClass), method);
		//获取原方法的ShadowMatch
		ShadowMatch originalShadowMatch = getShadowMatch(method, method);

		ProxyMethodInvocation pmi = null;
		Object targetObject = null;
		Object thisObject = null;
		try {
			MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
			targetObject = mi.getThis();
			//如果动态的匹配，那么MethodInvocation一定是一个代理对象
			if (!(mi instanceof ProxyMethodInvocation)) {
				throw new IllegalStateException("MethodInvocation不是summer的代理对象: " + mi);
			}
			pmi = (ProxyMethodInvocation) mi;
			//获取这个方法所在的代理对象
			thisObject = pmi.getProxy();
		}
		catch (IllegalStateException ex) {
			// No current invocation...
//			if (logger.isDebugEnabled()) {
//				logger.debug("Could not access current invocation - matching with limited context: " + ex);
//			}
		}

		try {
			//获取连接点
			JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);

			/*
			 最后一次检查是否存在this的差异匹配
			 使用原方法或者代理方法的ShadowMatch来保证有正确的检查this。
			 否则就无法得到正确的匹配
			 详情见SPR-2979
			 */
			if (pmi != null && thisObject != null) {  //
				RuntimeTestWalker originalMethodResidueTest = getRuntimeTestWalker(originalShadowMatch);
				if (!originalMethodResidueTest.testThisInstanceOfResidue(thisObject.getClass())) {
					return false;
				}
				if (joinPointMatch.matches()) {
					bindParameters(pmi, joinPointMatch);
				}
			}

			return joinPointMatch.matches();
		}
		catch (Throwable ex) {
//			if (logger.isDebugEnabled()) {
//				logger.debug("Failed to evaluate join point for arguments " + Arrays.asList(args) +
//						" - falling back to non-match", ex);
//			}
			return false;
		}
	}

	@Nullable
	protected String getCurrentProxiedBeanName() {
//		return ProxyCreationContext.getCurrentProxiedBeanName();
		return "";
	}


	/**
	 * 使用这个目标类的加载器创建切入点表达式
	 * 用于在匹配查验的时候 或者在获取ShadowMatch的时候 发生错误的补偿措施
	 */
	@Nullable
	private PointcutExpression getFallbackPointcutExpression(Class<?> targetClass) {
		try {
			//获取类加载器
			ClassLoader classLoader = targetClass.getClassLoader();
			//目标类的加载器不为空 并且 不等于切入点加载器
			if (classLoader != null && classLoader != this.pointcutClassLoader) {
				//使用这个类加载器创建切点表达式
				return buildPointcutExpression(classLoader);
			}
		}
		catch (Throwable ex) {
//			logger.debug("Failed to create fallback PointcutExpression", ex);
		}
		return null;
	}

	private RuntimeTestWalker getRuntimeTestWalker(ShadowMatch shadowMatch) {
		if (shadowMatch instanceof DefensiveShadowMatch) {
			return new RuntimeTestWalker(((DefensiveShadowMatch) shadowMatch).primary);
		}
		return new RuntimeTestWalker(shadowMatch);
	}

	private void bindParameters(ProxyMethodInvocation invocation, JoinPointMatch jpm) {
		// Note: Can't use JoinPointMatch.getClass().getName() as the key, since
		// Spring AOP does all the matching at a join point, and then all the invocations
		// under this scenario, if we just use JoinPointMatch as the key, then
		// 'last man wins' which is not what we want at all.
		// Using the expression is guaranteed to be safe, since 2 identical expressions
		// are guaranteed to bind in exactly the same way.
		invocation.setUserAttribute(resolveExpression(), jpm);
	}

	/**
	 * 获取ShadowMatch
	 * @param targetMethod 目标方法
	 * @param originalMethod 用来匹配的原方法
	 * @return
	 */
	private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
		// 通过并发访问避免已知方法的锁争用,先在缓存中查找
		ShadowMatch shadowMatch = this.shadowMatchCache.get(targetMethod);
		if (shadowMatch == null) {
			//shadowMatch的缓存容器
			synchronized (this.shadowMatchCache) {
				//之前没有找到，现在锁定后再次检查
				PointcutExpression fallbackExpression = null;
				Method methodToMatch = targetMethod;
				shadowMatch = this.shadowMatchCache.get(targetMethod);
				if (shadowMatch == null) {
					try {
						try {
							// 获取切点表达式,并做匹配判断,结果保存到ShadowMatch对象中
							shadowMatch = obtainPointcutExpression().matchesMethodExecution(methodToMatch);
						}
						catch (ReflectionWorldException ex) {
							// 如果匹配失败,可能是因为使用了特殊的类加载器,则尝试使用该特殊的类加载器替换掉默认的类加载器
							try {
								fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
								if (fallbackExpression != null) {
									shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
								}
							}
							catch (ReflectionWorldException ex2) {
								fallbackExpression = null;
							}
						}
						//如果依旧没有获取到shadowMatch，并且希望调用的方法并不是传入的方法（matches()方法中传入的参数），
						if (shadowMatch == null && targetMethod != originalMethod) {
							//尝试使用传入的方法再起创建shadowMatch
							methodToMatch = originalMethod;
							try {
								shadowMatch = obtainPointcutExpression().matchesMethodExecution(methodToMatch);
							}
							catch (ReflectionWorldException ex3) {
								try {
									fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
									if (fallbackExpression != null) {
										shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
									}
								}
								catch (ReflectionWorldException ex4) {
									fallbackExpression = null;
								}
							}
						}
					}
					catch (Throwable ex) {
//						logger.debug("PointcutExpression matching rejected target method", ex);
						fallbackExpression = null;
					}
					if (shadowMatch == null) {
						//最终没有能成功创建shadowMatch,那就new一个吧,封装成不匹配到
						shadowMatch = new ShadowMatchImpl(org.aspectj.util.FuzzyBoolean.NO, null, null, null);
					}
					else if (shadowMatch.maybeMatches() && fallbackExpression != null) {
						//如果通过匹配结果无法立即判断当前方法是否与目标方法匹配，
						//就将匹配得到的ShadowMatch和回调的ShadowMatch封装到DefensiveShadowMatch中
						shadowMatch = new DefensiveShadowMatch(shadowMatch,
								fallbackExpression.matchesMethodExecution(methodToMatch));
					}
					this.shadowMatchCache.put(targetMethod, shadowMatch);
				}
			}
		}
		return shadowMatch;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AspectJExpressionPointcut)) {
			return false;
		}
		AspectJExpressionPointcut otherPc = (AspectJExpressionPointcut) other;
		return ObjectUtils.nullSafeEquals(this.getExpression(), otherPc.getExpression()) &&
				ObjectUtils.nullSafeEquals(this.pointcutDeclarationScope, otherPc.pointcutDeclarationScope) &&
				ObjectUtils.nullSafeEquals(this.pointcutParameterNames, otherPc.pointcutParameterNames) &&
				ObjectUtils.nullSafeEquals(this.pointcutParameterTypes, otherPc.pointcutParameterTypes);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(this.getExpression());
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.pointcutDeclarationScope);
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.pointcutParameterNames);
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.pointcutParameterTypes);
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AspectJExpressionPointcut: ");
		sb.append("(");
		for (int i = 0; i < this.pointcutParameterTypes.length; i++) {
			sb.append(this.pointcutParameterTypes[i].getName());
			sb.append(" ");
			sb.append(this.pointcutParameterNames[i]);
			if ((i+1) < this.pointcutParameterTypes.length) {
				sb.append(", ");
			}
		}
		sb.append(")");
		sb.append(" ");
		if (getExpression() != null) {
			sb.append(getExpression());
		}
		else {
			sb.append("<pointcut expression not set>");
		}
		return sb.toString();
	}


	/**
	 * Handler for the Spring-specific {@code bean()} pointcut designator
	 * extension to AspectJ.
	 * <p>This handler must be added to each pointcut object that needs to
	 * handle the {@code bean()} PCD. Matching context is obtained
	 * automatically by examining a thread local variable and therefore a matching
	 * context need not be set on the pointcut.
	 */
	private class BeanPointcutDesignatorHandler implements PointcutDesignatorHandler {

		private static final String BEAN_DESIGNATOR_NAME = "bean";

		@Override
		public String getDesignatorName() {
			return BEAN_DESIGNATOR_NAME;
		}

		@Override
		public ContextBasedMatcher parse(String expression) {
			return new BeanContextMatcher(expression);
		}
	}


	/**
	 * Matcher class for the BeanNamePointcutDesignatorHandler.
	 * <p>Dynamic match tests for this matcher always return true,
	 * since the matching decision is made at the proxy creation time.
	 * For static match tests, this matcher abstains to allow the overall
	 * pointcut to match even when negation is used with the bean() pointcut.
	 */
	private class BeanContextMatcher implements ContextBasedMatcher {

		private final NamePattern expressionPattern;

		public BeanContextMatcher(String expression) {
			this.expressionPattern = new NamePattern(expression);
		}

		@Override
		@SuppressWarnings("rawtypes")
		@Deprecated
		public boolean couldMatchJoinPointsInType(Class someClass) {
			return (contextMatch(someClass) == FuzzyBoolean.YES);
		}

		@Override
		@SuppressWarnings("rawtypes")
		@Deprecated
		public boolean couldMatchJoinPointsInType(Class someClass, MatchingContext context) {
			return (contextMatch(someClass) == FuzzyBoolean.YES);
		}

		@Override
		public boolean matchesDynamically(MatchingContext context) {
			return true;
		}

		@Override
		public FuzzyBoolean matchesStatically(MatchingContext context) {
			return contextMatch(null);
		}

		@Override
		public boolean mayNeedDynamicTest() {
			return false;
		}

		private FuzzyBoolean contextMatch(@Nullable Class<?> targetType) {
//			String advisedBeanName = getCurrentProxiedBeanName();
//			if (advisedBeanName == null) {  // no proxy creation in progress
//				// abstain; can't return YES, since that will make pointcut with negation fail
//				return FuzzyBoolean.MAYBE;
//			}
//			if (BeanFactoryUtils.isGeneratedBeanName(advisedBeanName)) {
//				return FuzzyBoolean.NO;
//			}
//			if (targetType != null) {
//				boolean isFactory = FactoryBean.class.isAssignableFrom(targetType);
//				return FuzzyBoolean.fromBoolean(
//						matchesBean(isFactory ? BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName : advisedBeanName));
//			}
//			else {
//				return FuzzyBoolean.fromBoolean(matchesBean(advisedBeanName) ||
//						matchesBean(BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName));
//			}
			return FuzzyBoolean.MAYBE;
		}

		private boolean matchesBean(String advisedBeanName) {
//			return BeanFactoryAnnotationUtils.isQualifierMatch(
//					this.expressionPattern::matches, advisedBeanName, beanFactory);
			return true;
		}
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization, just initialize state after deserialization.
		ois.defaultReadObject();

		// Initialize transient fields.
		// pointcutExpression will be initialized lazily by checkReadyToMatch()
		this.shadowMatchCache = new ConcurrentHashMap<>(32);
	}


	private static class DefensiveShadowMatch implements ShadowMatch {

		private final ShadowMatch primary;

		private final ShadowMatch other;

		public DefensiveShadowMatch(ShadowMatch primary, ShadowMatch other) {
			this.primary = primary;
			this.other = other;
		}

		@Override
		public boolean alwaysMatches() {
			return this.primary.alwaysMatches();
		}

		@Override
		public boolean maybeMatches() {
			return this.primary.maybeMatches();
		}

		@Override
		public boolean neverMatches() {
			return this.primary.neverMatches();
		}

		@Override
		public JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args) {
			try {
				return this.primary.matchesJoinPoint(thisObject, targetObject, args);
			}
			catch (ReflectionWorldException ex) {
				return this.other.matchesJoinPoint(thisObject, targetObject, args);
			}
		}

		@Override
		public void setMatchingContext(MatchingContext aMatchContext) {
			this.primary.setMatchingContext(aMatchContext);
			this.other.setMatchingContext(aMatchContext);
		}
	}
}