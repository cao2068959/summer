package com.chy.summer.framework.core.ordered;

import com.chy.summer.framework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class OrderUtils {

    private static final Map<Class<?>, Object> orderCache = new ConcurrentHashMap<>(64);

    private static final Object NOT_ANNOTATED = new Object();

    public static int getOrder(Class<?> type, int defaultOrder) {
        Integer order = getOrder(type);
        return (order != null ? order : defaultOrder);
    }


    public static Integer getOrder(Class<?> type) {
        Object cached = orderCache.get(type);
        if (cached != null) {
            return (cached instanceof Integer ? (Integer) cached : null);
        }
        Order order = AnnotationUtils.findAnnotation(type, Order.class);
        Integer result = null;
        if (order != null) {
            result = order.value();
        }
        else {
            //TODO 在spring里面 还能使用 JDK提供的 注解Priority 来指定顺序,这里先跳过不考虑
            //result = getPriority(type);
        }
        orderCache.put(type, (result != null ? result : NOT_ANNOTATED));
        return result;
    }

}
