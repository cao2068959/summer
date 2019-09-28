package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 合成ClassFilter的静态工具类
 */
public abstract class ClassFilters {

	/**
	 * 将两个类过滤器合并成一个
	 * 新的类过滤器可以同时支持原先两个类过滤器的匹配对象
	 */
	public static ClassFilter union(ClassFilter cf1, ClassFilter cf2) {
		Assert.notNull(cf1, "参数1的ClassFilter不可为空");
		Assert.notNull(cf2, "参数2的ClassFilter不可为空");
		return new UnionClassFilter(new ClassFilter[] {cf1, cf2});
	}

	/**
	 * 将多个类过滤器合并成一个
	 * 新的类过滤器可以同时支持原先所有类过滤器的匹配对象
	 */
	public static ClassFilter union(ClassFilter[] classFilters) {
		Assert.notEmpty(classFilters, "ClassFilter不可为空");
		return new UnionClassFilter(classFilters);
	}

	/**
	 * 将两个类过滤器合并成一个
	 * 新的类过滤器只能匹配原先两个类过滤器的交集
	 */
	public static ClassFilter intersection(ClassFilter cf1, ClassFilter cf2) {
		Assert.notNull(cf1, "参数1的ClassFilter不可为空");
		Assert.notNull(cf2, "参数2的ClassFilter不可为空");
		return new IntersectionClassFilter(new ClassFilter[] {cf1, cf2});
	}

	/**
	 * 将多个类过滤器合并成一个
	 * 新的类过滤器只能匹配原先所有类过滤器的交集
	 */
	public static ClassFilter intersection(ClassFilter[] classFilters) {
		Assert.notEmpty(classFilters, "ClassFilter不可为空");
		return new IntersectionClassFilter(classFilters);
	}


	/**
	 * 并集类过滤器
	 */
	private static class UnionClassFilter implements ClassFilter, Serializable {
		/**
		 * 过滤器的集合
		 */
		private ClassFilter[] filters;

		/**
		 * 初始化并集过滤器
		 */
		public UnionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		/**
		 * 并集过滤器检定目标类型
		 * @param clazz clazz候选目标类型
		 * @return
		 */
		@Override
		public boolean matches(Class<?> clazz) {
			//循环匹配，只要一个能匹配上就能通过
			for (ClassFilter filter : this.filters) {
				if (filter.matches(clazz)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean equals(Object other) {
			return (this == other || (other instanceof UnionClassFilter &&
					ObjectUtils.nullSafeEquals(this.filters, ((UnionClassFilter) other).filters)));
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.filters);
		}
	}


	/**
	 * 交集类过滤器
	 */
	@SuppressWarnings("serial")
	private static class IntersectionClassFilter implements ClassFilter, Serializable {
		/**
		 * 过滤器的集合
		 */
		private ClassFilter[] filters;

		/**
		 * 初始化交集过滤器
		 */
		public IntersectionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		/**
		 * 交集过滤器检定目标类型
		 * @param clazz clazz候选目标类型
		 * @return
		 */
		@Override
		public boolean matches(Class<?> clazz) {
			for (ClassFilter filter : this.filters) {
				if (!filter.matches(clazz)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object other) {
			return (this == other || (other instanceof IntersectionClassFilter &&
					ObjectUtils.nullSafeEquals(this.filters, ((IntersectionClassFilter) other).filters)));
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.filters);
		}
	}

}