package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.*;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.interceptor.ExposeInvocationInterceptor;
import com.chy.summer.framework.aop.support.ComposablePointcut;
import com.chy.summer.framework.aop.support.MethodMatchers;
import com.chy.summer.framework.aop.support.StaticMethodMatcher;
import com.chy.summer.framework.core.DefaultParameterNameDiscoverer;
import com.chy.summer.framework.core.ParameterNameDiscoverer;
import com.chy.summer.framework.util.*;
import javax.annotation.Nullable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装AspectJ切面或带有AspectJ注释方法的Advice类的基类。
 */
public abstract class AbstractAspectJAdvice implements Advice, AspectJPrecedenceInformation, Serializable {

	/**
	 * 使用连接点的名称作为ReflectiveMethodInvocation映射中的key
	 */
	protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();


	/**
	 * 延迟实例化当前调用的联接点。
	 * 要求MethodInvocation与ExposeInvocationInterceptor绑定。
	 * @return 当前的AspectJ的连接点，如果我们不在AOP调用中，则异常。
	 */
	public static JoinPoint currentJoinPoint() {
		//获取当前调用的AOP关联的MethodInvocation对象
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		if (!(mi instanceof ProxyMethodInvocation)) {
			//这个对象一定是代理对象，否则无法延迟实例化
			throw new IllegalStateException("MethodInvocation不是Summer的代理MethodInvocation: " + mi);
		}
		ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
		//根据连接点名称 获取连接点
		JoinPoint jp = (JoinPoint) pmi.getUserAttribute(JOIN_POINT_KEY);
		if (jp == null) {
			//如果没有获取到连接点，我们将采用非常常规手段，在AOP运行的时候获取取到调用中的方法，进而获取到连接点对象
			jp = new MethodInvocationProceedingJoinPoint(pmi);
			pmi.setUserAttribute(JOIN_POINT_KEY, jp);
		}
		return jp;
	}


	private final Class<?> declaringClass;

	/**
	 * advice方法的名称
	 */
	private final String methodName;

	/**
	 * advice方法的参数类型
	 */
	private final Class<?>[] parameterTypes;

	/**
	 * advice方法本身
	 */
	protected transient Method aspectJAdviceMethod;

	/**
	 * 表达式切入点
	 */
	private final AspectJExpressionPointcut pointcut;

	/**
	 * 切面实例的工厂
	 */
	private final AspectInstanceFactory aspectInstanceFactory;

	/**
	 * 定义此advice的切面的名称（在确定建议优先级时使用，以便可以确定两条advice是否来自同一切面）。
	 */
	private String aspectName = "";

	/**
	 * 该advice在切面中的优先级顺序。
	 */
	private int declarationOrder;

	/**
	 * 参数名
	 * 如果此advice对象的创建者知道参数名称并显式设置它们，则该参数有值
	 */
	@Nullable
	private String[] argumentNames;

	/**
	 * 如果throwingAdvice绑定了抛出的值，则参数有值
	 */
	@Nullable
	private String throwingName;

	/**
	 * 如果returningAdvice绑定了返回的值，则参数有值
	 */
	@Nullable
	private String returningName;

	/**
	 * 返回参数的类型
	 */
	private Class<?> discoveredReturningType = Object.class;

	/**
	 * 抛出参数的类型
	 */
	private Class<?> discoveredThrowingType = Object.class;

	/**
	 * 连接方法参数的索引
	 * 仅有索引为0的时候受支持
	 */
	private int joinPointArgumentIndex = -1;

	/**
	 * JoinPointStaticPart的参数索引
	 * 仅有索引为0的时候受支持
	 */
	private int joinPointStaticPartArgumentIndex = -1;

	/**
	 * 参数名与参数索引的映射关系
	 */
	@Nullable
	private Map<String, Integer> argumentBindings;

	/**
	 * 是否绑定过参数
	 */
	private boolean argumentsIntrospected = false;

	/**
	 * 返回的通用类型
	 * 与discoveredReturningType获取的类型不同，discoveredReturningGenericType可以获取到泛型中的详细类型
	 * discoveredReturningType：java.util.List
	 * discoveredReturningGenericType：java.util.List<java.util.Date>
	 */
	@Nullable
	private Type discoveredReturningGenericType;
	// 与返回类型不同，因为Java不允许对异常类型进行参数化，所以抛出类型不需要此类通用信息。解释了为什么没有discoveredThrowingGenericType


	/**
	 * 为给定的通知方法创建一个新的AbstractAspectJAdvice
	 * @param aspectJAdviceMethod AspectJ风格的通知方法
	 * @param pointcut AspectJ表达式切入点
	 * @param aspectInstanceFactory 切面实例的工厂
	 */
	public AbstractAspectJAdvice(
			Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {

		Assert.notNull(aspectJAdviceMethod, "aspectJAdviceMethod不能为空");
		this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
		this.methodName = aspectJAdviceMethod.getName();
		this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.pointcut = pointcut;
		this.aspectInstanceFactory = aspectInstanceFactory;
	}


	/**
	 * 获得spectJ风格的通知方法
	 */
	public final Method getAspectJAdviceMethod() {
		return this.aspectJAdviceMethod;
	}

	/**
	 * 获得表达式切入点
	 */
	public final AspectJExpressionPointcut getPointcut() {
		//计算绑定参数
		calculateArgumentBindings();
		return this.pointcut;
	}

	/**
	 * 构建一个排除AspectJAdvice方法本身的“安全”切入点。
	 */
	public final Pointcut buildSafePointcut() {
		Pointcut pc = getPointcut();
		MethodMatcher safeMethodMatcher = MethodMatchers.intersection(
				new AdviceExcludingMethodMatcher(this.aspectJAdviceMethod), pc.getMethodMatcher());
		//根据给定的classFilter和MethodMatcher创建一个组合切入点
		return new ComposablePointcut(pc.getClassFilter(), safeMethodMatcher);
	}

	/**
	 * 获取切面实例的工厂
	 */
	public final AspectInstanceFactory getAspectInstanceFactory() {
		return this.aspectInstanceFactory;
	}

	/**
	 * 获取切面实例的类加载器
	 */
	@Nullable
	public final ClassLoader getAspectClassLoader() {
		return this.aspectInstanceFactory.getAspectClassLoader();
	}

	@Override
	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}


	public void setAspectName(String name) {
		this.aspectName = name;
	}

	/**
	 * 获取切面名称
	 * @return
	 */
	@Override
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 设置切面的调用优先度
	 */
	public void setDeclarationOrder(int order) {
		this.declarationOrder = order;
	}

	@Override
	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	/**
	 * 设置参数名
	 * 如果参数名称已知，则由此advice对象的创建者设置。
	 * 例如，这可能是因为它们已在XML或建议注释中明确指定。
	 */
	public void setArgumentNames(String argNames) {
		String[] tokens = StringUtils.commaDelimitedListToStringArray(argNames);
		setArgumentNamesFromStringArray(tokens);
	}

	/**
	 * 根据string数组生成参数名
	 */
	public void setArgumentNamesFromStringArray(String... args) {
		//初始化参数名数组
		this.argumentNames = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			//设置参数名
			this.argumentNames[i] = StringUtils.trimWhitespace(args[i]);
			if (!isVariableName(this.argumentNames[i])) {
				throw new IllegalArgumentException(
						"AbstractAspectJAdvice的argumentNames属性包含参数名称'" +
						this.argumentNames[i] + "' 该参数名称不符合java变量名规范");
			}
		}
		if (this.argumentNames != null) {
			if (this.aspectJAdviceMethod.getParameterCount() == this.argumentNames.length + 1) {
				// 可能需要添加隐式连接点参数名称
				Class<?> firstArgType = this.aspectJAdviceMethod.getParameterTypes()[0];
				if (firstArgType == JoinPoint.class ||
						firstArgType == ProceedingJoinPoint.class ||
						firstArgType == JoinPoint.StaticPart.class) {
					//如果参数首位是JoinPoint、ProceedingJoinPoint、JoinPoint.StaticPart需要把首位参数的填补上去
					String[] oldNames = this.argumentNames;
					this.argumentNames = new String[oldNames.length + 1];
					this.argumentNames[0] = "THIS_JOIN_POINT";
					System.arraycopy(oldNames, 0, this.argumentNames, 1, oldNames.length);
				}
			}
		}
	}

	public void setReturningName(String name) {
		throw new UnsupportedOperationException("只有afterReturningAdvice可以用于绑定返回值");
	}

	/**
	 * 为了进行参数绑定计算，我们需要在此级别保留返回名称，此方法允许afterReturning通知子类设置名称
	 */
	protected void setReturningNameNoCheck(String name) {
		// 这个名称可以变量也可能是类型
		if (isVariableName(name)) {
			this.returningName = name;
		}
		else {
			//假设是个类型
			try {
				this.discoveredReturningType = ClassUtils.forName(name, getAspectClassLoader());
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("返回名称 '" + name + "'既不是有效的参数名称，也不是类路径上Java类型的标准名称。 原因: " + ex);
			}
		}
	}

	/**
	 * 获取返回参数的类型
	 */
	protected Class<?> getDiscoveredReturningType() {
		return this.discoveredReturningType;
	}

	/**
	 * 返回的通用类型
	 */
	@Nullable
	protected Type getDiscoveredReturningGenericType() {
		return this.discoveredReturningGenericType;
	}

	public void setThrowingName(String name) {
		throw new UnsupportedOperationException("只有afterThrowingAdvice建议可用于绑定抛出的异常");
	}

	/**
	 * 为了进行参数绑定计算，我们需要在此级别保留抛出名称，此方法允许afterThrowing通知子类设置名称。
	 */
	protected void setThrowingNameNoCheck(String name) {
		// 这个名称可以变量也可能是类型
		if (isVariableName(name)) {
			this.throwingName = name;
		}
		else {
			// 假设是个类型
			try {
				this.discoveredThrowingType = ClassUtils.forName(name, getAspectClassLoader());
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("抛出名称 '" + name + "'既不是有效的参数名称，也不是类路径上Java类型的标准名称。 原因: " + ex);
			}
		}
	}

	/**
	 * 获得抛出参数的类型
	 */
	protected Class<?> getDiscoveredThrowingType() {
		return this.discoveredThrowingType;
	}

	/**
	 * 判断变量名是否合法
	 */
	private boolean isVariableName(String name) {
		char[] chars = name.toCharArray();
		//判断变量首位是否符合java规范
		if (!Character.isJavaIdentifierStart(chars[0])) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			//判断变量后续是否符合java规范
			if (!Character.isJavaIdentifierPart(chars[i])) {
				return false;
			}
		}
		return true;
	}


	/**
	 * 计算绑定的参数
	 */
	public synchronized final void calculateArgumentBindings() {
		//如果已经进行过参数绑定了  或者通知方法中没有参数
		if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
			return;
		}

		int numUnboundArgs = this.parameterTypes.length;
		//通知方法参数类型
		Class<?>[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
		if (maybeBindJoinPoint(parameterTypes[0]) || maybeBindProceedingJoinPoint(parameterTypes[0])) {
			//如果第一个参数是JoinPoint或者ProceedingJoinPoint
			//maybeBindJoinPoint()和maybeBindProceedingJoinPoint()会设置对应的索引，并且ProceedingJoinPoint还会判断是否是环绕通知
			numUnboundArgs--;
		}
		else if (maybeBindJoinPointStaticPart(parameterTypes[0])) {
			//如果第一个参数是JoinPoint.StaticPart
			numUnboundArgs--;
		}

		if (numUnboundArgs > 0) {
			// 需要通过切入点匹配返回的名称绑定参数
			bindArgumentsByName(numUnboundArgs);
		}
		//设定已绑定过参数
		this.argumentsIntrospected = true;
	}

	/**
	 * 判断绑定的参数是JoinPoint类型的
	 */
	private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
		if (JoinPoint.class == candidateParameterType) {
			//设置joinPoint参数所在的索引
			this.joinPointArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 判断绑定的参数是ProceedingJoinPoint类型的
	 */
	private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
		if (ProceedingJoinPoint.class == candidateParameterType) {
			if (!supportsProceedingJoinPoint()) {
				throw new IllegalArgumentException("ProceedingJoinPoint仅支持环绕通知");
			}
			//设置ProceedingJoinPoint参数所在的索引
			this.joinPointArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean supportsProceedingJoinPoint() {
		return false;
	}

	/**
	 * 判断绑定的参数是JoinPointStaticPart类型的
	 */
	private boolean maybeBindJoinPointStaticPart(Class<?> candidateParameterType) {
		if (JoinPoint.StaticPart.class == candidateParameterType) {
			this.joinPointStaticPartArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 根据参数名绑定参数
	 * 将参数名和参数索引位置绑定
	 * @param numArgumentsExpectingToBind 希望绑定的参数个数
	 */
	private void bindArgumentsByName(int numArgumentsExpectingToBind) {
		if (this.argumentNames == null) {
			//获取方法的参数名
			this.argumentNames = createParameterNameDiscoverer().getParameterNames(this.aspectJAdviceMethod);
		}
		if (this.argumentNames != null) {
			//绑定参数
			bindExplicitArguments(numArgumentsExpectingToBind);
		}
		else {
			throw new IllegalStateException("通知方法 [" + this.aspectJAdviceMethod.getName() + "] " +
					"的" + numArgumentsExpectingToBind + " 个参数要通过名称绑定，但是参数名称未指定且无法发现。");
		}
	}

	/**
	 * 创建一个ParameterNameDiscoverer以用于参数绑定。
	 * 默认实现创建一个DefaultParameterNameDiscoverer，并添加一个专门配置的AspectJAdviceParameterNameDiscoverer。
	 */
	protected ParameterNameDiscoverer createParameterNameDiscoverer() {
		// 并不一定能百分百的确定参数的名称，如果无法确定我们就当做失败来处理
		DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
		//参数名发现器，用来查找Advice的方法参数
		AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer =
				new AspectJAdviceParameterNameDiscoverer(this.pointcut.getExpression());
		adviceParameterNameDiscoverer.setReturningName(this.returningName);
		adviceParameterNameDiscoverer.setThrowingName(this.throwingName);
		adviceParameterNameDiscoverer.setRaiseExceptions(true);
		discoverer.addDiscoverer(adviceParameterNameDiscoverer);
		return discoverer;
	}

	/**
	 * 参数名与参数索引绑定
	 * @param numArgumentsLeftToBind
	 */
	private void bindExplicitArguments(int numArgumentsLeftToBind) {
		Assert.state(this.argumentNames != null, "没有可用的参数名称");
		this.argumentBindings = new HashMap<>();

		//希望找到的参数名个数
		int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterCount();
		if (this.argumentNames.length != numExpectedArgumentNames) {
			throw new IllegalStateException("根据advice的参数希望找到" + numExpectedArgumentNames +
					" 个参数名, 但实际上找到了" + this.argumentNames.length + " 个参数");
		}

		// 如果我们在数量上相匹配，计算参数名的索引偏移量，在calculateArgumentBindings()方法中我们会特殊处理掉部分参数
		int argumentIndexOffset = this.parameterTypes.length - numArgumentsLeftToBind;
		for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
			//将参数名于索引相互绑定
			this.argumentBindings.put(this.argumentNames[i], i);
		}

		// 查询返回名称和抛出名称是否在列表当中，然找到对应的参数类型
		if (this.returningName != null) {
			//判断参数里是否包含返回参数名
			if (!this.argumentBindings.containsKey(this.returningName)) {
				throw new IllegalStateException("返回参数名称'" + this.returningName + "'未绑定在advice参数中");
			}
			else {
				//获取到参数名绑定的索引
				Integer index = this.argumentBindings.get(this.returningName);
				//设置返回参数的类型
				this.discoveredReturningType = this.aspectJAdviceMethod.getParameterTypes()[index];
				this.discoveredReturningGenericType = this.aspectJAdviceMethod.getGenericParameterTypes()[index];

			}
		}
		if (this.throwingName != null) {
			if (!this.argumentBindings.containsKey(this.throwingName)) {
				throw new IllegalStateException("抛出参数名称 '" + this.throwingName +
						"' 未绑定在advice参数中");
			}
			else {
				//获取到参数名绑定的索引
				Integer index = this.argumentBindings.get(this.throwingName);
				//设置抛出参数的类型
				this.discoveredThrowingType = this.aspectJAdviceMethod.getParameterTypes()[index];
			}
		}

		//配置相应地切入点表达式
		configurePointcutParameters(this.argumentNames, argumentIndexOffset);
	}

	/**
	 * 在偏移量之后的参数都是切入点参数，但是返回参数和抛出参数的变量处理方式不太相同
	 * 如果存在的话，必须将其从列表中删除
	 */
	private void configurePointcutParameters(String[] argumentNames, int argumentIndexOffset) {
		int numParametersToRemove = argumentIndexOffset;
		if (this.returningName != null) {
			numParametersToRemove++;
		}
		if (this.throwingName != null) {
			numParametersToRemove++;
		}
		//排除完参数的参数名数组
		String[] pointcutParameterNames = new String[argumentNames.length - numParametersToRemove];
		//排除完参数的参数类型数组
		Class<?>[] pointcutParameterTypes = new Class<?>[pointcutParameterNames.length];
		//获取方法的类型
		Class<?>[] methodParameterTypes = this.aspectJAdviceMethod.getParameterTypes();

		int index = 0;
		//排除returningName和throwingName
		for (int i = 0; i < argumentNames.length; i++) {
			if (i < argumentIndexOffset) {
				continue;
			}
			if (argumentNames[i].equals(this.returningName) ||
				argumentNames[i].equals(this.throwingName)) {
				continue;
			}
			pointcutParameterNames[index] = argumentNames[i];
			pointcutParameterTypes[index] = methodParameterTypes[i];
			index++;
		}

		//将排除之后的数组赋值给原先对象
		this.pointcut.setParameterNames(pointcutParameterNames);
		this.pointcut.setParameterTypes(pointcutParameterTypes);
	}

	/**
	 * 将参数值与参数索引绑定
	 * 之前的方法中将参数名与参数索引绑定过了
	 * 在方法执行连接点处获取参数，并将一组参数输出到advice方法
	 * @param jp 当前的连接点
	 * @param jpMatch 用于该执行连接点匹配的连接点匹配器
	 * @param returnValue 方法执行的返回值（可以为空）
	 * @param ex 方法执行引发的异常（可以为空）
	 */
	protected Object[] argBinding(JoinPoint jp, @Nullable JoinPointMatch jpMatch,
			@Nullable Object returnValue, @Nullable Throwable ex) {
		//计算绑定参数
		calculateArgumentBindings();

		// AMC start
		//advice调用的参数
		Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
		//绑定数量
		int numBound = 0;
		if (this.joinPointArgumentIndex != -1) {
			//当首位参数是JoinPoint类型的时候
			adviceInvocationArgs[this.joinPointArgumentIndex] = jp;
			numBound++;
		}
		else if (this.joinPointStaticPartArgumentIndex != -1) {
			//当首位参数是JoinPointStaticPart类型的时候
			adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = jp.getStaticPart();
			numBound++;
		}

		if (!CollectionUtils.isEmpty(this.argumentBindings)) {
			//从切入点匹配进行绑定
			if (jpMatch != null) {
				//参数绑定数组
				PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
				for (PointcutParameter parameter : parameterBindings) {
					//获取参数名
					String name = parameter.getName();
					//获取参数索引
					Integer index = this.argumentBindings.get(name);
					//将参数索引于参数对象绑定
					adviceInvocationArgs[index] = parameter.getBinding();
					numBound++;
				}
			}
			// 绑定返回对象
			if (this.returningName != null) {
				//获取参数索引
				Integer index = this.argumentBindings.get(this.returningName);
				//将参数索引于参数对象绑定
				adviceInvocationArgs[index] = returnValue;
				numBound++;
			}
			// 绑定抛出对象
			if (this.throwingName != null) {
				Integer index = this.argumentBindings.get(this.throwingName);
				adviceInvocationArgs[index] = ex;
				numBound++;
			}
		}

		if (numBound != this.parameterTypes.length) {
			throw new IllegalStateException("需要绑定" + this.parameterTypes.length +
					" 个参数，但在调用中有" + numBound + "个参数绑定"+(jpMatch == null ? "失败" : "成功"));

		}

		return adviceInvocationArgs;
	}


	/**
	 * 调用advice方法
	 * @param jpMatch 用于该执行连接点匹配的连接点匹配器
	 * @param returnValue 方法执行的返回值（可以为空）
	 * @param ex 方法执行引发的异常（可以为空）
	 */
	protected Object invokeAdviceMethod(
			@Nullable JoinPointMatch jpMatch, @Nullable Object returnValue, @Nullable Throwable ex)
			throws Throwable {

		return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
	}

	/**
	 * 调用advice方法
	 */
	protected Object invokeAdviceMethod(JoinPoint jp, @Nullable JoinPointMatch jpMatch,
			@Nullable Object returnValue, @Nullable Throwable t) throws Throwable {

		return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
	}

	/**
	 * 使用给定的参数调用advice方法
	 */
	protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
		Object[] actualArgs = args;
		if (this.aspectJAdviceMethod.getParameterCount() == 0) {
			actualArgs = null;
		}
		try {
			//将需要调用的方法设置成可访问
			ReflectionUtils.makeAccessible(this.aspectJAdviceMethod);
			//调用方法执行
			return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("advice方法参数不匹配 [" +
					this.aspectJAdviceMethod + "]; 切入点表达 [" +
					this.pointcut.getPointcutExpression() + "]", ex);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	/**
	 * 获取连接点
	 */
	protected JoinPoint getJoinPoint() {
		return currentJoinPoint();
	}

	/**
	 * 获取连接点的匹配器
	 */
	@Nullable
	protected JoinPointMatch getJoinPointMatch() {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		if (!(mi instanceof ProxyMethodInvocation)) {
			throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
		}
		return getJoinPointMatch((ProxyMethodInvocation) mi);
	}

	/*
		不能使用JoinPointMatch.getClass().getName()作为键，
		我们会在连接点处进行所有匹配，在这种情况下所有的连接点的关联都会被调用，
		如果我们仅使用JoinPointMatch作为键，则最后一个执行的就会成为最终的结果，然而那是错误的。
		保证使用表达式是安全的，因为可以保证两个完全相同的表达式以完全相同的方式绑定。
	 */

	/**
	 * 通过代理方法调用器，获取连接点匹配器
	 * @param pmi
	 * @return
	 */
	@Nullable
	protected JoinPointMatch getJoinPointMatch(ProxyMethodInvocation pmi) {
		String expression = this.pointcut.getExpression();
		return (expression != null ? (JoinPointMatch) pmi.getUserAttribute(expression) : null);
	}


	@Override
	public String toString() {
		return getClass().getName() + ": advice method [" + this.aspectJAdviceMethod + "]; " +
				"aspect name '" + this.aspectName + "'";
	}

	/**
	 * 序列化支持
	 */
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
	 * 排除指定Advice方法的MethodMatcher
	 */
	private static class AdviceExcludingMethodMatcher extends StaticMethodMatcher {
		/**
		 * 通知方法
		 */
		private final Method adviceMethod;

		public AdviceExcludingMethodMatcher(Method adviceMethod) {
			this.adviceMethod = adviceMethod;
		}

		/**
		 * 判断给定方法是否是通知方法
		 * @return
		 */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass) {
			return !this.adviceMethod.equals(method);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AdviceExcludingMethodMatcher)) {
				return false;
			}
			AdviceExcludingMethodMatcher otherMm = (AdviceExcludingMethodMatcher) other;
			return this.adviceMethod.equals(otherMm.adviceMethod);
		}

		@Override
		public int hashCode() {
			return this.adviceMethod.hashCode();
		}
	}

}