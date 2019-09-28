package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.util.StringUtils;

import java.lang.annotation.Annotation;

public class AliasForTask {


    private final String formName;
    private  Class<? extends Annotation> formClass;

    private Class<? extends Annotation> targerClass;
    private String targerName;

    public AliasForTask(AliasFor aliasFor,String defaultName,Class<? extends Annotation> annotationClass) {
        targerClass =aliasFor.annotation();
        targerName = aliasFor.name();
        //如果没显性的设置属性的名称,就默认和方法名称一致
        if(StringUtils.isEmpty(targerName)){
            targerName = defaultName;
        }

        formClass = annotationClass;
        formName = defaultName;

    }


    public String getFormName() {
        return formName;
    }

    public Class<? extends Annotation> getFormClass() {
        return formClass;
    }

    public void setFormClass(Class<? extends Annotation> formClass) {
        this.formClass = formClass;
    }

    public Class<? extends Annotation> getTargerClass() {
        return targerClass;
    }

    public void setTargerClass(Class<? extends Annotation> targerClass) {
        this.targerClass = targerClass;
    }

    public String getTargerName() {
        return targerName;
    }

    public void setTargerName(String targerName) {
        this.targerName = targerName;
    }
}
