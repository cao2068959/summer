package chy.test.annotation;

import chy.test.service.ImprotExec2;
import com.chy.summer.framework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ImprotExec2.class)
public @interface ImportC {
}
