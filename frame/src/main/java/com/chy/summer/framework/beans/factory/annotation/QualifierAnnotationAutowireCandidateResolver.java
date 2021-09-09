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
        Object value = findValue(descriptor.getAnnotations());
        //TODO暂时不支持 方法注入方式下的 @Value 注解
        return value;
    }

    protected Object findValue(Annotation[] annotationsToSearch) {

        if (annotationsToSearch != null && annotationsToSearch.length > 0) {
            AnnotationAttributes attr = AnnotatedElementUtils.getMergedAnnotationAttributes(
                    AnnotatedElementUtils.forAnnotations(annotationsToSearch), this.valueAnnotationType);
            if (attr != null) {
                //去获取 @Value 注解中的 value值
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
     *
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
