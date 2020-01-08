package com.chy.summer.framework.context.annotation.condition;


import com.chy.summer.framework.core.type.AnnotationBehavior;

public interface Condition {


    boolean matches(ConditionContext context, AnnotationBehavior annotationBehavior);
}
