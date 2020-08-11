package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.MutablePropertyValues;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.factory.ConstructorArgumentValues;
import com.chy.summer.framework.context.annotation.constant.Autowire;
import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.ClassUtils;
import lombok.Getter;
import lombok.Setter;


import java.util.function.Supplier;

public abstract class AbstractBeanDefinition extends AttributeAccessorSupport implements BeanDefinition {

    @Getter
    @Setter
    private ScopeType scope = ScopeType.SINGLETON;

    @Getter
    @Setter
    private boolean lazyInit = false;

    @Getter
    private boolean synthetic = false;

    @Getter
    @Setter
    private Resource resource;

    private boolean abstractFlag = false;

    @Setter
    private volatile Object beanClass;

    @Setter
    @Getter
    private Autowire autowireMode = Autowire.NO;

    @Getter
    @Setter
    private String initMethodName;

    @Setter
    @Getter
    private String destroyMethodName;

    @Setter
    private Supplier<?> instanceSupplier;

    @Setter
    private String factoryMethodName;

    /** 代表了 这个beanDefinition 里的所有属性 */
    private MutablePropertyValues propertyValues;

    /**
     * 构造器里所有参数放在这里
     */
    @Getter
    @Setter
    private ConstructorArgumentValues constructorArgumentValues;


    protected AbstractBeanDefinition(BeanDefinition original) {
        setBeanClassName(original.getBeanClassName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());


        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            setPrimary(originalAbd.isPrimary());
            setResource(originalAbd.getResource());
            setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
        }

    }

    public AbstractBeanDefinition() {
    }


    @Override
    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        }
        else {
            return (String) beanClassObject;
        }
    }

    @Override
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    private void setAbstract(boolean anAbstract) {
        this.abstractFlag = anAbstract;
    }


    @Override
    public boolean isSingleton() {
        return ScopeType.SINGLETON == this.scope;
    }


    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append("]");
        sb.append("; scope=").append(this.scope);
        sb.append("; lazyInit=").append(this.lazyInit);
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }


    @Override
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }



    public Class<?> resolveBeanClass( ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    public boolean hasMethodOverrides() {
        return false;
    }


    @Override
    public MutablePropertyValues getPropertyValues() {
        if (this.propertyValues == null) {
            this.propertyValues = new MutablePropertyValues();
        }
        return this.propertyValues;
    }

//    public void copyQualifiersFrom(AbstractBeanDefinition source) {
//        Assert.notNull(source, "Source不可为空");
//        this.qualifiers.putAll(source.qualifiers);
//    }


    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }
}
