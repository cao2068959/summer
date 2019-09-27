package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.core.DefaultParameterNameDiscoverer;
import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.core.ParameterNameDiscoverer;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * ProceedingJoinPoint接口的实现，该接口包装了AOP Alliance的MethodInvocation。
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint, JoinPoint.StaticPart {

	/**
	 * 参数发现器
	 */
	private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	/**
	 * 方法调用器
	 */
	private final ProxyMethodInvocation methodInvocation;

	/**
	 * 方法执行参数的缓存
	 */
	@Nullable
	private Object[] defensiveCopyOfArgs;

	/**
	 * 延迟初始化的签名对象
	 */
	@Nullable
	private Signature signature;

	/**
	 * 延迟初始化的源位置对象
	 */
	@Nullable
	private SourceLocation sourceLocation;


	/**
	 * 创建一个新的MethodInvocationProceedingJoinPoint
	 */
	public MethodInvocationProceedingJoinPoint(ProxyMethodInvocation methodInvocation) {
		Assert.notNull(methodInvocation, "MethodInvocation不能为空");
		this.methodInvocation = methodInvocation;
	}

	@Override
	public void set$AroundClosure(AroundClosure aroundClosure) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 使用方法执行器的克隆对象执行方法
	 */
	@Override
	public Object proceed() throws Throwable {
		return this.methodInvocation.invocableClone().proceed();
	}

	@Override
	public Object proceed(Object[] arguments) throws Throwable {
		Assert.notNull(arguments, "传递来的参数数组不能为空");
		if (arguments.length != this.methodInvocation.getArguments().length) {
			throw new IllegalArgumentException("执行方法的参数为 " +
					this.methodInvocation.getArguments().length + "个, " +
					"但是传递来的参数只有" + arguments.length + " 个");
		}
		this.methodInvocation.setArguments(arguments);
		return this.methodInvocation.invocableClone(arguments).proceed();
	}

	/**
	 * 获取aop的代理对象，并给不可以为空
	 */
	@Override
	public Object getThis() {
		return this.methodInvocation.getProxy();
	}

	/**
	 * 获取AOP的目标对象。 如果没有目标，则可能为空
	 */
	@Override
	@Nullable
	public Object getTarget() {
		return this.methodInvocation.getThis();
	}

	@Override
	public Object[] getArgs() {
		if (this.defensiveCopyOfArgs == null) {
			//获取方法执行的参数
			Object[] argsSource = this.methodInvocation.getArguments();
			//初始化参数缓存
			this.defensiveCopyOfArgs = new Object[argsSource.length];
			//拷贝参数
			System.arraycopy(argsSource, 0, this.defensiveCopyOfArgs, 0, argsSource.length);
		}
		return this.defensiveCopyOfArgs;
	}

	//获取签名
	@Override
	public Signature getSignature() {
		if (this.signature == null) {
			this.signature = new MethodSignatureImpl();
		}
		return signature;
	}

	/**
	 * 获取源位置
	 * @return
	 */
	@Override
	public SourceLocation getSourceLocation() {
		if (this.sourceLocation == null) {
			this.sourceLocation = new SourceLocationImpl();
		}
		return this.sourceLocation;
	}

	/**
	 * 获取类
	 * @return METHOD_EXECUTION表示方法执行器
	 */
	@Override
	public String getKind() {
		return ProceedingJoinPoint.METHOD_EXECUTION;
	}

	@Override
	public int getId() {
		// 适配器
		return 0;
	}

	@Override
	public JoinPoint.StaticPart getStaticPart() {
		return this;
	}

	@Override
	public String toShortString() {
		return "execution(" + getSignature().toShortString() + ")";
	}

	@Override
	public String toLongString() {
		return "execution(" + getSignature().toLongString() + ")";
	}

	@Override
	public String toString() {
		return "execution(" + getSignature().toString() + ")";
	}


	/**
	 * 延迟初始化的Method签名
	 */
	private class MethodSignatureImpl implements MethodSignature {

		/**
		 * 参数名称
		 */
		@Nullable
		private volatile String[] parameterNames;

		/**
		 * 获取方法名
		 */
		@Override
		public String getName() {
			return methodInvocation.getMethod().getName();
		}

		/**
		 * 获取修饰类型
		 */
		@Override
		public int getModifiers() {
			return methodInvocation.getMethod().getModifiers();
		}

		/**
		 * 获取声明类型
		 */
		@Override
		public Class<?> getDeclaringType() {
			return methodInvocation.getMethod().getDeclaringClass();
		}

		/**
		 * 获取声明类型名称
		 */
		@Override
		public String getDeclaringTypeName() {
			return methodInvocation.getMethod().getDeclaringClass().getName();
		}

		/**
		 * 获取返回类型
		 */
		@Override
		public Class<?> getReturnType() {
			return methodInvocation.getMethod().getReturnType();
		}

		/**
		 * 获取执行方法
		 */
		@Override
		public Method getMethod() {
			return methodInvocation.getMethod();
		}

		/**
		 * 获取参数类型
		 */
		@Override
		public Class<?>[] getParameterTypes() {
			return methodInvocation.getMethod().getParameterTypes();
		}

		/**
		 * 获取参数名称
		 */
		@Override
		@Nullable
		public String[] getParameterNames() {
			if (this.parameterNames == null) {
				this.parameterNames = parameterNameDiscoverer.getParameterNames(getMethod());
			}
			return this.parameterNames;
		}

		/**
		 * 获取抛出异常的类型
		 */
		@Override
		public Class<?>[] getExceptionTypes() {
			return methodInvocation.getMethod().getExceptionTypes();
		}

		@Override
		public String toShortString() {
			return toString(false, false, false, false);
		}

		@Override
		public String toLongString() {
			return toString(true, true, true, true);
		}

		@Override
		public String toString() {
			return toString(false, true, false, true);
		}

		private String toString(boolean includeModifier, boolean includeReturnTypeAndArgs,
				boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {

			StringBuilder sb = new StringBuilder();
			if (includeModifier) {
				sb.append(Modifier.toString(getModifiers()));
				sb.append(" ");
			}
			if (includeReturnTypeAndArgs) {
				appendType(sb, getReturnType(), useLongReturnAndArgumentTypeName);
				sb.append(" ");
			}
			appendType(sb, getDeclaringType(), useLongTypeName);
			sb.append(".");
			sb.append(getMethod().getName());
			sb.append("(");
			Class<?>[] parametersTypes = getParameterTypes();
			appendTypes(sb, parametersTypes, includeReturnTypeAndArgs, useLongReturnAndArgumentTypeName);
			sb.append(")");
			return sb.toString();
		}

		/**
		 * 处理toString中的字符串
		 */
		private void appendTypes(StringBuilder sb, Class<?>[] types, boolean includeArgs,
				boolean useLongReturnAndArgumentTypeName) {

			if (includeArgs) {
				for (int size = types.length, i = 0; i < size; i++) {
					appendType(sb, types[i], useLongReturnAndArgumentTypeName);
					if (i < size - 1) {
						sb.append(",");
					}
				}
			}
			else {
				if (types.length != 0) {
					sb.append("..");
				}
			}
		}

		private void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
			if (type.isArray()) {
				appendType(sb, type.getComponentType(), useLongTypeName);
				sb.append("[]");
			}
			else {
				sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
			}
		}
	}


	/**
	 * 延迟初始化的源位置
	 */
	private class SourceLocationImpl implements SourceLocation {

		@Override
		public Class<?> getWithinType() {
			if (methodInvocation.getThis() == null) {
				throw new UnsupportedOperationException("没有可用的连接点：目标为null");
			}
			//当前连接点的类型
			return methodInvocation.getThis().getClass();
		}

		@Override
		public String getFileName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getLine() {
			throw new UnsupportedOperationException();
		}

		@Override
		@Deprecated
		public int getColumn() {
			throw new UnsupportedOperationException();
		}
	}

}