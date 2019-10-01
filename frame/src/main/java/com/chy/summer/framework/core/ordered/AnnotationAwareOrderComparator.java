package com.chy.summer.framework.core.ordered;

import com.chy.summer.framework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class AnnotationAwareOrderComparator extends OrderComparator {

    public static final AnnotationAwareOrderComparator INSTANCE = new AnnotationAwareOrderComparator();


    /**
     * 如果 Ordered 不是以 接口实现的方式,而是以 注解的方式那么走这里
     * @param obj
     * @return
     */
    @Override
    protected Integer findOrder(Object obj) {

        //如果他 实现了对应的接口,那么就直接放行
        Integer order = super.findOrder(obj);
        if (order != null) {
            return order;
        }

        // 如果这个对象是一个普通的类,就去检查一下他 父类 接口 上是否有 @Order 注解, 包括注解的父类也会去检查
        if (obj instanceof Class) {
            return OrderUtils.getOrder((Class<?>) obj);
        }

        //如果这个注解是打在了方法上面
        else if (obj instanceof Method) {
            Order ann = AnnotationUtils.findAnnotation((Method) obj, Order.class);
            if (ann != null) {
                return ann.value();
            }
        } else {
            order = OrderUtils.getOrder(obj.getClass());
        }

        return order;
    }



}
