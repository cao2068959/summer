package com.chy.summer.framework.beans.factory.annotation;

import com.chy.summer.framework.annotation.beans.Qualifier;
import com.chy.summer.framework.annotation.beans.Value;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.factory.AutowireCandidateResolver;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.core.MethodParameter;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.AnnotatedElementUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 注入的时候,用来处理 @Qualifier 注解
 */
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver {

    private final Set<Class<? extends Annotation>> qualifierTypes = new LinkedHashSet<>(2);

    private Class<? extends Annotation> valueAnnotationType = Value.class;

    public QualifierAnnotationAutowireCandidateResolver() {
        this.qualifierTypes.add(Qualifier.class);
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return true;
    }


    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        //去寻找这个 属性/方法 有没打了 @Value 注解,如果有的话,把他里面的值给拿出来
        Object value = findValue(descriptor.getAnnotations());

        //如果 上面的没有找到对应的@Value 注解,那么可以这个注解是打在了,方法的参数里,顺便去拿一下
        if (value == null) {
            MethodParameter methodParam = descriptor.getMethodParameter();
            if (methodParam != null) {
                value = findValue(methodParam.getParameterAnnotations());
            }
        }
        return value;
    }


    /**
     * 从所有的注解中 查找有没一个叫做 @Value 的注解,并且返回这个注解里设置的值
     *
     * @param annotationsToSearch
     * @return
     */
    protected Object findValue(Annotation[] annotationsToSearch) {
        if (annotationsToSearch.length > 0) {
            AnnotationAttributes attr = AnnotatedElementUtils
                    .getMergedAnnotationAttributes(AnnotatedElementUtils.forAnnotations(annotationsToSearch), this.valueAnnotationType);
            if (attr != null) {
                return extractValue(attr);
            }
        }
        return null;
    }


    protected Object extractValue(AnnotationAttributes attr) {
        Object value = attr.getAttributeValue("value");
        if (value == null) {
            throw new IllegalStateException("Value annotation must have a value attribute");
        }
        return value;
    }


    /**
     * 判断这个注解是不是 @Qualifier
     * @param annotationType
     * @return
     */
    protected boolean isQualifier(Class<? extends Annotation> annotationType) {
        for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
            if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
                return true;
            }
        }
        return false;
    }
}
