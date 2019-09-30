package com.chy.summer.framework.context.annotation.utils;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.context.annotation.Lazy;
import com.chy.summer.framework.context.annotation.ScopeMetadata;
import com.chy.summer.framework.context.annotation.constant.ScopedProxyMode;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.type.AnnotationMetadata;

/**
 * 用了解析annotation 的一个公用的工具类
 */
public class AnnotationConfigUtils {

    /**
     *  检查类上面是否还有一些其他影响类行为的注解把他对应设置的值放入BeanDefinition
     *  这里在spring里面解析了 @Lazy @Primary @DependsOn @Role  @Description
     *  TODO 这里只会先实现 @Lazy 后面留坑
     */
    public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition definition,
                                                          AnnotationMetadata metadata) {

        //如果存在 @Lazy 注解就做对应的操作
        AnnotationAttributes lazyAttributes = metadata.getAnnotationAttributes(Lazy.class);
        if(lazyAttributes != null){
            Boolean isLazy = lazyAttributes.getRequiredAttribute("value", Boolean.class);
            definition.setLazyInit(isLazy);
        }
    }

    /**
     * 如果在 @Scope 注解中设置了 代理模式，
     * 那么这里会从 beanDefinitionHolder中拿到BeanDefinition
     * 然后弄一个新的 RootBeanDefinition 把原来的BeanDefinition 的属性搞过去，同时设置一些属性
     * 最后把这个这个 RootBeanDefinition 塞到一个beanDefinitionHolder中，返回回去
     * 同时他的beanName 前面会加上 scopedTarget.
     */
    public static BeanDefinitionHolder applyScopedProxyMode(ScopeMetadata scopeMetadata,
                                                            BeanDefinitionHolder beanDefinitionHolder,
                                                            BeanDefinitionRegistry registry) {

        ScopedProxyMode scopedProxyMode = scopeMetadata.getScopedProxyMode();
        if(scopedProxyMode == ScopedProxyMode.NO){
            return beanDefinitionHolder;
        }

        //TODO 如果真设置了代理模式的，先留一个坑，后面慢慢填
        return beanDefinitionHolder;
    }
}
