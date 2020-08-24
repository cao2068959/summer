package com.chy.summer.framework.beans.factory.annotation;

import com.chy.summer.framework.annotation.beans.Qualifier;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.factory.AutowireCandidateResolver;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
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

    public QualifierAnnotationAutowireCandidateResolver() {
        this.qualifierTypes.add(Qualifier.class);
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return true;
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
