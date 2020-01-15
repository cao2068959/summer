package chy.test.conditionTest;


import com.chy.summer.framework.context.annotation.condition.Condition;
import com.chy.summer.framework.context.annotation.condition.ConditionContext;
import com.chy.summer.framework.core.type.AnnotationBehavior;

public class Cb implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotationBehavior annotationBehavior) {
        return true;
    }
}
