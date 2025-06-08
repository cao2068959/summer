package com.chy.summer.framework.core.ordered;

import com.chy.summer.framework.core.PriorityOrdered;
import javax.annotation.Nullable;

import java.util.Comparator;

/**
 * 用于 Order 接口排序
 */
public class OrderComparator implements Comparator<Object> {

    public static final OrderComparator INSTANCE = new OrderComparator();


    @Override
    public int compare(Object o1, Object o2) {
        return doCompare(o1, o2);
    }

    private int doCompare(@Nullable Object o1, @Nullable Object o2) {
        boolean p1 = (o1 instanceof PriorityOrdered);
        boolean p2 = (o2 instanceof PriorityOrdered);

        //PriorityOrdered接口一定在 其他类型接口的前面
        if (p1 && !p2) {
            return -1;
        }
        else if (p2 && !p1) {
            return 1;
        }

        int i1 = getOrder(o1);
        int i2 = getOrder(o2);
        return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
    }


    protected int getOrder(@Nullable Object obj) {
        if (obj != null) {
            Integer order = findOrder(obj);
            if (order != null) {
                return order;
            }
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    protected Integer findOrder(Object obj) {
        return (obj instanceof Ordered ? ((Ordered) obj).getOrder() : null);
    }

}
