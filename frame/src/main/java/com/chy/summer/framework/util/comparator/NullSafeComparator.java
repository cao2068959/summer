package com.chy.summer.framework.util.comparator;

import com.chy.summer.framework.util.Assert;
import javax.annotation.Nullable;

import java.util.Comparator;

/**
 * 一个比较器，可以安全地比较空值是否比其他对象低或高
 * 可以装饰给定的比较器或对可比对象进行处理
 */
public class NullSafeComparator<T> implements Comparator<T> {

	/**
	 * 比较器的共享默认实例，将空值视为比非空对象低的值
	 */
	public static final NullSafeComparator NULLS_LOW = new NullSafeComparator<>(true);

	/**
	 * 比较器的共享默认实例，将空值视为比非空对象高的值
	 */
	public static final NullSafeComparator NULLS_HIGH = new NullSafeComparator<>(false);

	/**
	 * 非空比较器
	 */
	private final Comparator<T> nonNullComparator;

	/**
	 * 空值比较条件，true：空值最小，false：空值最大
	 */
	private final boolean nullsLow;


	/**
	 * 创建一个新的空值比较器，使用默认的比较器
	 * nonNullComparator用于比较两个非空对象，nullsLow用于比较空值对象
	 * @param nullsLow true：空值最小，false：空值最大
	 */
	private NullSafeComparator(boolean nullsLow) {
		this.nonNullComparator = ComparableComparator.INSTANCE;
		this.nullsLow = nullsLow;
	}

	/**
	 * 使用指定的空值比较器，创建一个新的空值比较器
	 * Comparator will be applied to.
	 * @param comparator 用于比较两个非空对象
	 * @param nullsLow true：空值最小，false：空值最大
	 */
	public NullSafeComparator(Comparator<T> comparator, boolean nullsLow) {
		Assert.notNull(comparator, "必须为非空的比较器");
		this.nonNullComparator = comparator;
		this.nullsLow = nullsLow;
	}

	/**
	 * 比较器
	 */
	@Override
	public int compare(@Nullable T o1, @Nullable T o2) {
		if (o1 == o2) {
			return 0;
		}
		if (o1 == null) {
			return (this.nullsLow ? -1 : 1);
		}
		if (o2 == null) {
			return (this.nullsLow ? 1 : -1);
		}
		return this.nonNullComparator.compare(o1, o2);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NullSafeComparator)) {
			return false;
		}
		NullSafeComparator<T> other = (NullSafeComparator<T>) obj;
		return (this.nonNullComparator.equals(other.nonNullComparator) && this.nullsLow == other.nullsLow);
	}

	@Override
	public int hashCode() {
		return this.nonNullComparator.hashCode() * (this.nullsLow ? -1 : 1);
	}

	@Override
	public String toString() {
		return "NullSafeComparator: 非空比较器 [" + this.nonNullComparator + "]; " +
				(this.nullsLow ? "空值最小" : "空值最大");
	}

}