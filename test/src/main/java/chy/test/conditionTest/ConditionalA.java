package chy.test.conditionTest;

import com.chy.summer.framework.context.annotation.condition.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(Ca.class)
public @interface ConditionalA {
}
