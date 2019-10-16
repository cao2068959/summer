package com.chy.summer.framework.beans;

import com.chy.summer.framework.util.ObjectUtils;

public class BeanWrapperImpl implements BeanWrapper {

    Object wrappedObject;

    private String nestedPath = "";


    Object rootObject;

    public BeanWrapperImpl(Object beanInstance) {
        setWrappedInstance(beanInstance,"",null);
    }

    public void setWrappedInstance(Object object, String nestedPath,  Object rootObject) {
        //剥除 Optional
        this.wrappedObject = ObjectUtils.unwrapOptional(object);
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!"".equals(this.nestedPath) ? rootObject : this.wrappedObject);
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
