package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.IntroductionAwareMethodMatcher;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.ProxyMethodInvocation;
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

	private String[] pointcutParameterNames = new String[0];

	private Class<?>[] pointcutParameterTypes = new Class<?>[0];

	@Nullable
	private BeanFactory beanFactory;

	@Nullable
	private transient ClassLoader pointcutClassLoader;

	@Nullable
	private transient PointcutExpression pointcutExpression;

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
	 * 懒加载的方式创建底层aspectj切入点表达式
	 */
	private PointcutExpression obtainPointcutExpression() {
		if (getExpression() == null) {
			throw new IllegalStateException("在匹配之前必须设置属性“expression”");
		}
		if (this.pointcutExpression == null) {
			this.pointcutClassLoader = determinePointcutClassLoader();
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
	//TODO 写到这里了
	/**
	 * Build the underlying AspectJ pointcut expression.
	 */
	private PointcutExpression buildPointcutExpression(@Nullable ClassLoader classLoader) {
		PointcutParser parser = initializePointcutParser(classLoader);
		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
		for (int i = 0; i < pointcutParameters.length; i++) {
			pointcutParameters[i] = parser.createPointcutParameter(
					this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
		}
		return parser.parsePointcutExpression(replaceBooleanOperators(resolveExpression()),
				this.pointcutDeclarationScope, pointcutParameters);
	}

	private String resolveExpression() {
		String expression = getExpression();
		Assert.state(expression != null, "No expression set");
		return expression;
	}

	/**
	 * Initialize the underlying AspectJ pointcut parser.
	 */
	private PointcutParser initializePointcutParser(@Nullable ClassLoader classLoader) {
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
						SUPPORTED_PRIMITIVES, classLoader);
		parser.registerPointcutDesignatorHandler(new BeanPointcutDesignatorHandler());
		return parser;
	}


	/**
	 * If a pointcut expression has been specified in XML, the user cannot
	 * write {@code and} as "&&" (though &amp;&amp; will work).
	 * We also allow {@code and} between two pointcut sub-expressions.
	 * <p>This method converts back to {@code &&} for the AspectJ pointcut parser.
	 */
	private String replaceBooleanOperators(String pcExpr) {
		String result = StringUtils.replace(pcExpr, " and ", " && ");
		result = StringUtils.replace(result, " or ", " || ");
		result = StringUtils.replace(result, " not ", " ! ");
		return result;
	}


	/**
	 * Return the underlying AspectJ pointcut expression.
	 */
	public PointcutExpression getPointcutExpression() {
		return obtainPointcutExpression();
	}

	@Override
	public boolean matches(Class<?> targetClass) {
		PointcutExpression pointcutExpression = obtainPointcutExpression();
		try {
			try {
				return pointcutExpression.couldMatchJoinPointsInType(targetClass);
			}
			catch (ReflectionWorld.ReflectionWorldException ex) {
//				logger.debug("PointcutExpression matching rejected target class - trying fallback expression", ex);
				// Actually this is still a "maybe" - treat the pointcut as dynamic if we don't know enough yet
				PointcutExpression fallbackExpression = getFallbackPointcutExpression(targetClass);
				if (fallbackExpression != null) {
					return fallbackExpression.couldMatchJoinPointsInType(targetClass);
				}
			}
		}
		catch (Throwable ex) {
//			logger.debug("PointcutExpression matching rejected target class", ex);
		}
		return false;
	}

	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass, boolean beanHasIntroductions) {
//		obtainPointcutExpression();
//		Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
//		ShadowMatch shadowMatch = getShadowMatch(targetMethod, method);
//
//		// Special handling for this, target, @this, @target, @annotation
//		// in Spring - we can optimize since we know we have exactly this class,
//		// and there will never be matching subclass at runtime.
//		if (shadowMatch.alwaysMatches()) {
//			return true;
//		}
//		else if (shadowMatch.neverMatches()) {
//			return false;
//		}
//		else {
//			// the maybe case
//			if (beanHasIntroductions) {
//				return true;
//			}
//			// A match test returned maybe - if there are any subtype sensitive variables
//			// involved in the test (this, target, at_this, at_target, at_annotation) then
//			// we say this is not a match as in Spring there will never be a different
//			// runtime subtype.
//			RuntimeTestWalker walker = getRuntimeTestWalker(shadowMatch);
//			return (!walker.testsSubtypeSensitiveVars() ||
//					(targetClass != null && walker.testTargetInstanceOfResidue(targetClass)));
//		}
		return true;
	}

	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass) {
		return matches(method, targetClass, false);
	}

	@Override
	public boolean isRuntime() {
		return obtainPointcutExpression().mayNeedDynamicTest();
	}

	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
//		obtainPointcutExpression();
//		ShadowMatch shadowMatch = getShadowMatch(AopUtils.getMostSpecificMethod(method, targetClass), method);
//		ShadowMatch originalShadowMatch = getShadowMatch(method, method);
//
//		// Bind Spring AOP proxy to AspectJ "this" and Spring AOP target to AspectJ target,
//		// consistent with return of MethodInvocationProceedingJoinPoint
//		ProxyMethodInvocation pmi = null;
//		Object targetObject = null;
//		Object thisObject = null;
//		try {
//			MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
//			targetObject = mi.getThis();
//			if (!(mi instanceof ProxyMethodInvocation)) {
//				throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
//			}
//			pmi = (ProxyMethodInvocation) mi;
//			thisObject = pmi.getProxy();
//		}
//		catch (IllegalStateException ex) {
//			// No current invocation...
////			if (logger.isDebugEnabled()) {
////				logger.debug("Could not access current invocation - matching with limited context: " + ex);
////			}
//		}
//
//		try {
//			JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);
//
//			/*
//			 * Do a final check to see if any this(TYPE) kind of residue match. For
//			 * this purpose, we use the original method's (proxy method's) shadow to
//			 * ensure that 'this' is correctly checked against. Without this check,
//			 * we get incorrect match on this(TYPE) where TYPE matches the target
//			 * type but not 'this' (as would be the case of JDK dynamic proxies).
//			 * <p>See SPR-2979 for the original bug.
//			 */
//			if (pmi != null && thisObject != null) {  // there is a current invocation
//				RuntimeTestWalker originalMethodResidueTest = getRuntimeTestWalker(originalShadowMatch);
//				if (!originalMethodResidueTest.testThisInstanceOfResidue(thisObject.getClass())) {
//					return false;
//				}
//				if (joinPointMatch.matches()) {
//					bindParameters(pmi, joinPointMatch);
//				}
//			}
//
//			return joinPointMatch.matches();
//		}
//		catch (Throwable ex) {
////			if (logger.isDebugEnabled()) {
////				logger.debug("Failed to evaluate join point for arguments " + Arrays.asList(args) +
////						" - falling back to non-match", ex);
////			}
//			return false;
//		}
		return true;
	}

	@Nullable
	protected String getCurrentProxiedBeanName() {
//		return ProxyCreationContext.getCurrentProxiedBeanName();
		return "";
	}


	/**
	 * Get a new pointcut expression based on a target class's loader rather than the default.
	 */
	@Nullable
	private PointcutExpression getFallbackPointcutExpression(Class<?> targetClass) {
		try {
			ClassLoader classLoader = targetClass.getClassLoader();
			if (classLoader != null && classLoader != this.pointcutClassLoader) {
				return buildPointcutExpression(classLoader);
			}
		}
		catch (Throwable ex) {
//			logger.debug("Failed to create fallback PointcutExpression", ex);
		}
		return null;
	}

//	private RuntimeTestWalker getRuntimeTestWalker(ShadowMatch shadowMatch) {
//		if (shadowMatch instanceof DefensiveShadowMatch) {
//			return new RuntimeTestWalker(((DefensiveShadowMatch) shadowMatch).primary);
//		}
//		return new RuntimeTestWalker(shadowMatch);
//	}

	private void bindParameters(ProxyMethodInvocation invocation, JoinPointMatch jpm) {
		// Note: Can't use JoinPointMatch.getClass().getName() as the key, since
		// Spring AOP does all the matching at a join point, and then all the invocations
		// under this scenario, if we just use JoinPointMatch as the key, then
		// 'last man wins' which is not what we want at all.
		// Using the expression is guaranteed to be safe, since 2 identical expressions
		// are guaranteed to bind in exactly the same way.
		invocation.setUserAttribute(resolveExpression(), jpm);
	}

	private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
//		// Avoid lock contention for known Methods through concurrent access...
//		ShadowMatch shadowMatch = this.shadowMatchCache.get(targetMethod);
//		if (shadowMatch == null) {
//			synchronized (this.shadowMatchCache) {
//				// Not found - now check again with full lock...
//				PointcutExpression fallbackExpression = null;
//				Method methodToMatch = targetMethod;
//				shadowMatch = this.shadowMatchCache.get(targetMethod);
//				if (shadowMatch == null) {
//					try {
//						try {
//							shadowMatch = obtainPointcutExpression().matchesMethodExecution(methodToMatch);
//						}
//						catch (ReflectionWorldException ex) {
//							// Failed to introspect target method, probably because it has been loaded
//							// in a special ClassLoader. Let's try the declaring ClassLoader instead...
//							try {
//								fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
//								if (fallbackExpression != null) {
//									shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
//								}
//							}
//							catch (ReflectionWorldException ex2) {
//								fallbackExpression = null;
//							}
//						}
//						if (shadowMatch == null && targetMethod != originalMethod) {
//							methodToMatch = originalMethod;
//							try {
//								shadowMatch = obtainPointcutExpression().matchesMethodExecution(methodToMatch);
//							}
//							catch (ReflectionWorldException ex3) {
//								// Could neither introspect the target class nor the proxy class ->
//								// let's try the original method's declaring class before we give up...
//								try {
//									fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
//									if (fallbackExpression != null) {
//										shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
//									}
//								}
//								catch (ReflectionWorldException ex4) {
//									fallbackExpression = null;
//								}
//							}
//						}
//					}
//					catch (Throwable ex) {
//						// Possibly AspectJ 1.8.10 encountering an invalid signature
//						logger.debug("PointcutExpression matching rejected target method", ex);
//						fallbackExpression = null;
//					}
//					if (shadowMatch == null) {
//						shadowMatch = new ShadowMatchImpl(org.aspectj.util.FuzzyBoolean.NO, null, null, null);
//					}
//					else if (shadowMatch.maybeMatches() && fallbackExpression != null) {
//						shadowMatch = new DefensiveShadowMatch(shadowMatch,
//								fallbackExpression.matchesMethodExecution(methodToMatch));
//					}
//					this.shadowMatchCache.put(targetMethod, shadowMatch);
//				}
//			}
//		}
//		return shadowMatch;
		return this.shadowMatchCache.get(targetMethod);
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