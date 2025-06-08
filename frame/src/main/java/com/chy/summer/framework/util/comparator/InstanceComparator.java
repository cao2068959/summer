package com.chy.summer.framework.util.comparator;

import com.chy.summer.framework.util.Assert;
import javax.annotation.Nullable;

import java.util.Comparator;

/**
 * 根据任意类顺序比较对象
 */
public class InstanceComparator<T> implements Comparator<T> {

	private final Class<?>[] instanceOrder;

	public InstanceComparator(Class<?>... instanceOrder) {
		Assert.notNull(instanceOrder, "'instanceOrder'数组不可为空");
		this.instanceOrder = instanceOrder;
	}


	@Override
	public int compare(T o1, T o2) {
		int i1 = getOrder(o1);
		int i2 = getOrder(o2);
		return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
	}

	/**
	 * 获取对象的优先级，根据instanceOrder列表的有序性获取优先级
	 */
	private int getOrder(@Nullable T object) {
		if (object != null) {
			for (int i = 0; i < this.instanceOrder.length; i++) {
				if (this.instanceOrder[i].isInstance(object)) {
					return i;
				}
			}
		}
		return this.instanceOrder.length;
	}

}