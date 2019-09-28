package com.chy.summer.framework.aop;

/**
 * AOP代理接口的标记，这些接口明确打算返回原始目标对象（从方法调用返回时，通常将其替换为代理对象）。
 * 请注意，这是标记样式的标记接口，声明的接口，而不是具体对象的完整类。
 * 此标记仅适用于特定接口（通常，introduction接口不作为AOP代理的主要接口），因此不会影响具体AOP代理可以实现的其他接口。
 */
public interface RawTargetAccess {

}