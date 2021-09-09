package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.beans.config.BeanDefinitionHolder;

public interface AutowireCandidateResolver {

    default boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return bdHolder.getBeanDefinition().isAutowireCandidate();
    }

    /**
     * 查找这个依赖有没有推荐的值，这个用于在注入属性的时候，可以设置一些自定义的 AutowireCandidateResolver
     * 来动态更改注入的值， 比如 @value 注解中的表达式，就是这个里面获取的
     *
     * 你也可以自己去实现一个自己的 AutowireCandidateResolver 来实现自定义的注解完成 自动注入
     *
     * @param descriptor
     */
    Object getSuggestedValue(DependencyDescriptor descriptor);
}
