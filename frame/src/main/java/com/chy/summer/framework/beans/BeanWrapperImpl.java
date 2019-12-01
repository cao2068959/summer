package com.chy.summer.framework.beans;

import com.chy.summer.framework.util.ObjectUtils;
import lombok.Getter;
import lombok.Setter;

public class BeanWrapperImpl implements BeanWrapper {

    private  Object wrappedObject;

    private String nestedPath = "";


    Object rootObject;

    public BeanWrapperImpl() {
    }

    public BeanWrapperImpl(Object beanInstance) {
        setWrappedInstance(beanInstance,"",null);
    }

    public void setWrappedInstance(Object object, String nestedPath,  Object rootObject) {
        //剥除 Optional
        this.wrappedObject = ObjectUtils.unwrapOptional(object);
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!"".equals(this.nestedPath) ? rootObject : this.wrappedObject);
    }

    public void setBeanInstance(Object object) {
        this.wrappedObject = object;
        this.rootObject = object;
    }


    @Override
    public Object getWrappedInstance() {
        return wrappedObject;
    }

    @Override
    public Class<?> getWrappedClass() {
        return getWrappedInstance().getClass();
    }
}
