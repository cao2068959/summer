package chy.test.annotation;

import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.annotation.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service("什么鬼")
public @interface ChyService {

    @AliasFor(annotation = Service.class)
    String value() default "";

    String data() default "222";
}
