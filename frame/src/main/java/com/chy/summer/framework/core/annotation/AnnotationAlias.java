package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;

public class AnnotationAlias {

    @Setter
    @Getter
    private  String formName;


    @Setter
    @Getter
    private Class<? extends Annotation> targerClass;

    @Setter
    @Getter
    private String targerName;

    public AnnotationAlias(AliasFor aliasFor, String defaultName, Class<? extends Annotation> annotationClass) {
        targerClass =aliasFor.annotation();
        //如果等于Annotation 说明他没有指定要继承到哪一个注解上面,那么就指向他自己
        if(targerClass == Annotation.class){
            targerClass = annotationClass;
        }

        targerName = aliasFor.name();
        //如果没显性的设置属性的名称,就默认和方法名称一致
        if(StringUtils.isEmpty(targerName)){
            targerName = defaultName;
        }

        formName = defaultName;

    }


}
