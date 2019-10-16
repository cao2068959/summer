package com.chy.summer.framework.util.comparator;

import java.util.Comparator;

/**
 * 比较器扩展的方便的入口点与通用类型的工厂方法
 */
public abstract class Comparators {

	/**
	 * 获取一个通用的比较器
	 */
	public static <T> Comparator<T> comparable() {
		return ComparableComparator.INSTANCE;
	}

	/**
	 * 获取一个空值比较器，空值最小
	 */
	public static <T> Comparator<T> nullsLow() {
		return NullSafeComparator.NULLS_LOW;
	}

	/**
	 * 使用指定的非空比较器，获取一个空值比较器，空值最小
	 */
	public static <T> Comparator<T> nullsLow(Comparator<T> comparator) {
		return new NullSafeComparator<T>(comparator, true);
	}

	/**
	 * 获取一个空值比较器，空值最大
	 */
	public static <T> Comparator<T> nullsHigh() {
		return NullSafeComparator.NULLS_HIGH;
	}

	/**
	 * 使用指定的非空比较器，获取一个空值比较器，空值最大
	 */
	public static <T> Comparator<T> nullsHigh(Comparator<T> comparator) {
		return new NullSafeComparator<T>(comparator, false);
	}

}