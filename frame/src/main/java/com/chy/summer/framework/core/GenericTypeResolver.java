package com.chy.summer.framework.core;


public abstract class GenericTypeResolver {

    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericIfc) {
        //获取genericIfc 对应类型的接口,没有就找父类和接口,直到找到
        ResolvableType resolvableType = ResolvableType.forClass(clazz).as(genericIfc);
        if (!resolvableType.hasGenerics()) {
            return null;
        }
        //获取第一个泛型的类型
        return getSingleGeneric(resolvableType);
    }

    private static Class<?> getSingleGeneric(ResolvableType resolvableType) {
        return resolvableType.getGeneric().resolve();
    }

}
