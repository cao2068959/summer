package com.chy.summer.framework.context.imported;


import com.chy.summer.framework.core.type.AnnotationMetadata;

import java.util.function.Predicate;

public interface ImportSelector {

    /**
     * 通过传入的类以及他的注解，来判断要导入的bean，返回对应的beanName
     *
     * @param importingClassMetadata
     * @return
     */
    String[] selectImports(AnnotationMetadata importingClassMetadata);

    /**
     * 返回要排除的类
     *
     * @return
     */
    default Predicate<String> getExclusionFilter() {
        return null;
    }

}
