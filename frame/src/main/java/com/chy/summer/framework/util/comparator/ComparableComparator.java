package com.chy.summer.framework.util.comparator;

import java.util.Comparator;

public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

	/**
	 * 默认比较器的共享实例,默认的比较方法
	 */
	public static final ComparableComparator INSTANCE = new ComparableComparator();


	@Override
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}