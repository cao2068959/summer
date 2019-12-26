package chy.test.conditionTest;

import com.chy.summer.framework.context.annotation.condition.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(Cb.class)
public @interface ConditionalB {
}
