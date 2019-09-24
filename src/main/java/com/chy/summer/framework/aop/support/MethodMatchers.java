package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.IntroductionAwareMethodMatcher;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 合成MethodMatcher的静态工具类
 */
public abstract class MethodMatchers {

    /**
     * 将两个方法匹配器合并成一个
     * 新的方法匹配器可以同时支持原先两个方法匹配器的匹配对象
     */
	public static MethodMatcher union(MethodMatcher mm1, MethodMatcher mm2) {
		return new UnionMethodMatcher(mm1, mm2);
	}

	/**
	 * 将两个方法匹配器合并成一个,同时设置两个方法匹配器对应的类过滤器
	 */
	static MethodMatcher union(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
		return new ClassFilterAwareUnionMethodMatcher(mm1, cf1, mm2, cf2);
	}

	/**
	 * 将两个方法匹配器合并成一个
     * 新的方法匹配器只能匹配原先两个方法匹配器的交集
	 */
	public static MethodMatcher intersection(MethodMatcher mm1, MethodMatcher mm2) {
		return new IntersectionMethodMatcher(mm1, mm2);
	}

	/**
	 * 将给定的方法匹配器应用于给定的方法
	 * @param mm 需要应用的方法匹配器
	 * @param method 指定方法
	 * @param targetClass 目标类
	 * @param hasIntroductions true：我们要代表的对象是一个或多个Introduction，false反之
	 */
	public static boolean matches(MethodMatcher mm, Method method, @Nullable Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(mm, "MethodMatcher不可为空");
		return ((mm instanceof IntroductionAwareMethodMatcher &&
				((IntroductionAwareMethodMatcher) mm).matches(method, targetClass, hasIntroductions)) ||
				mm.matches(method, targetClass));
	}


	/**
	 * 两个方法匹配器的并集
	 */
	private static class UnionMethodMatcher implements IntroductionAwareMethodMatcher, Serializable {

		private final MethodMatcher mm1;

		private final MethodMatcher mm2;

        /**
         * 初始化并集方法匹配器
         */
		public UnionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
			Assert.notNull(mm1, "参数1的MethodMatcher不可为空");
			Assert.notNull(mm2, "参数2的MethodMatcher不可为空");
			this.mm1 = mm1;
			this.mm2 = mm2;
		}

        /**
         * 匹配指定的方法，并且判断是否是Introduction
         */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass, boolean hasIntroductions) {
			return (matchesClass1(targetClass) && MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions)) ||
					(matchesClass2(targetClass) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
		}

        /**
         * 匹配指定的方法
         */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass) {
			return (matchesClass1(targetClass) && this.mm1.matches(method, targetClass)) ||
					(matchesClass2(targetClass) && this.mm2.matches(method, targetClass));
		}

        /**
         * 判断方法匹配器1的实例对象类型
         * 默认返回true,由子类实现
         */
		protected boolean matchesClass1(@Nullable Class<?> targetClass) {
			return true;
		}

        /**
         * 判断方法匹配器2的实例对象类型
         * 默认返回true,由子类实现
         */
		protected boolean matchesClass2(@Nullable Class<?> targetClass) {
			return true;
		}

        /**
         * 是否是动态方法匹配器
         */
		@Override
		public boolean isRuntime() {
			return this.mm1.isRuntime() || this.mm2.isRuntime();
		}

        /**
         * 严格的匹配
         *
         * @param method 带匹配的方法
         * @param targetClass 方法的实例对象的类型
         * @param args 方法参数
         * @return
         */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
			return this.mm1.matches(method, targetClass, args) || this.mm2.matches(method, targetClass, args);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof UnionMethodMatcher)) {
				return false;
			}
			UnionMethodMatcher that = (UnionMethodMatcher) obj;
			return (this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2));
		}

		@Override
		public int hashCode() {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.mm1.hashCode();
			hashCode = 37 * hashCode + this.mm2.hashCode();
			return hashCode;
		}
	}


	/**
	 * 方法匹配器实现，用于两个给定方法匹配器的并集，
     * 支持每个方法匹配器关联的类过滤器。
	 */
	private static class ClassFilterAwareUnionMethodMatcher extends UnionMethodMatcher {

		private final ClassFilter cf1;

		private final ClassFilter cf2;

		public ClassFilterAwareUnionMethodMatcher(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
			super(mm1, mm2);
			this.cf1 = cf1;
			this.cf2 = cf2;
		}

		@Override
		protected boolean matchesClass1(@Nullable Class<?> targetClass) {
			return (targetClass != null && this.cf1.matches(targetClass));
		}

		@Override
		protected boolean matchesClass2(@Nullable Class<?> targetClass) {
			return (targetClass != null && this.cf2.matches(targetClass));
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!super.equals(other)) {
				return false;
			}
			ClassFilter otherCf1 = ClassFilter.TRUE;
			ClassFilter otherCf2 = ClassFilter.TRUE;
			if (other instanceof ClassFilterAwareUnionMethodMatcher) {
				ClassFilterAwareUnionMethodMatcher cfa = (ClassFilterAwareUnionMethodMatcher) other;
				otherCf1 = cfa.cf1;
				otherCf2 = cfa.cf2;
			}
			return (this.cf1.equals(otherCf1) && this.cf2.equals(otherCf2));
		}
	}


	/**
	 * 两个方法匹配器的交集
	 */
	@SuppressWarnings("serial")
	private static class IntersectionMethodMatcher implements IntroductionAwareMethodMatcher, Serializable {

		private final MethodMatcher mm1;

		private final MethodMatcher mm2;

        /**
         * 初始化交集方法匹配器
         */
		public IntersectionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
            Assert.notNull(mm1, "参数1的MethodMatcher不可为空");
            Assert.notNull(mm2, "参数2的MethodMatcher不可为空");
			this.mm1 = mm1;
			this.mm2 = mm2;
		}

        /**
         * 匹配指定的方法，并且判断是否是Introduction
         */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass, boolean hasIntroductions) {
			return MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions) &&
					MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions);
		}

        /**
         * 匹配指定的方法
         */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass) {
			return this.mm1.matches(method, targetClass) && this.mm2.matches(method, targetClass);
		}

        /**
         * 是否是动态方法匹配器
         */
		@Override
		public boolean isRuntime() {
			return this.mm1.isRuntime() || this.mm2.isRuntime();
		}

        /**
         * 严格的匹配
         *
         * @param method 带匹配的方法
         * @param targetClass 方法的实例对象的类型
         * @param args 方法参数
         * @return
         */
		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
            /*
		    因为一个动态的交集可以由一个静态和动态部分组成，
            我们必须避免在动态匹配器上调用3个参数匹配方法
            一个不受支持的操作。
             */
			boolean aMatches = this.mm1.isRuntime() ?
					this.mm1.matches(method, targetClass, args) : this.mm1.matches(method, targetClass);
			boolean bMatches = this.mm2.isRuntime() ?
					this.mm2.matches(method, targetClass, args) : this.mm2.matches(method, targetClass);
			return aMatches && bMatches;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof IntersectionMethodMatcher)) {
				return false;
			}
			IntersectionMethodMatcher that = (IntersectionMethodMatcher) other;
			return (this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2));
		}

		@Override
		public int hashCode() {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.mm1.hashCode();
			hashCode = 37 * hashCode + this.mm2.hashCode();
			return hashCode;
		}
	}

}