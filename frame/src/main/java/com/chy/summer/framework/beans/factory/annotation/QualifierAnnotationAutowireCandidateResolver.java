package com.chy.summer.framework.beans.factory.annotation;

import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.factory.AutowireCandidateResolver;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;

import java.lang.annotation.Annotation;

/**
 * 注入的时候,用来处理 @Qualifier 注解
 */
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver {

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {

        //Boolean match = checkQualifiers(bdHolder, descriptor.getAnnotations());
        Boolean match = false;
        if (match) {
            /*MethodParameter methodParam = descriptor.getMethodParameter();
            if (methodParam != null) {
                Method method = methodParam.getMethod();
                if (method == null || void.class == method.getReturnType()) {
                    match = checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
                }
            }*/
        }
        return match;
    }


    /**
     * 用来检查 对应的bean 是否符合 @Qualifier 注解上所指定的bean
     * @param bdHolder
     * @param annotations
     * @return
     */
    private Boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotations) {
        return true;
    }
}
