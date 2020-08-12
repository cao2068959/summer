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

    /**
     * 作用域
     */
    @Getter
    @Setter
    private ScopeType scope = ScopeType.SINGLETON;

    /**
     * 是否懒加载
     */
    @Getter
    @Setter
    private boolean lazyInit = false;

    /**
     *  标识这个beandefinition 是 "人造" 的而不是程序自动生成的
     */
    @Getter
    private boolean synthetic = false;

    /**
     *  bean对象的原始数据
     */
    @Getter
    @Setter
    private Resource resource;

    /**
     * 是否是抽象类
     */
    private boolean abstractFlag = false;

    /**
     * 源对象bean所属的class
     */
    @Setter
    private volatile Object beanClass;

    /**
     * 是否自动注入
     */
    @Setter
    @Getter
    private Autowire autowireMode = Autowire.NO;

    /**
     * 初始化方法,在生成对应的bean对象之后,回去调用这个方法
     */
    @Getter
    @Setter
    private String initMethodName;

    /**
     * bean销毁后会去调用
     */
    @Setter
    @Getter
    private String destroyMethodName;


    /**
     * 工厂方法的名称
     */
    @Setter
    @Getter
    private String factoryMethodName;

    /** 代表了 这个beanDefinition 里的所有属性 */
    private MutablePropertyValues propertyValues;

    /**
     * 构造器里所有参数放在这里
     */
    @Setter
    private ConstructorArgumentValues constructorArgumentValues;


    @Getter
    @Setter
    private String[] dependsOn;

    /**
     * 如果上了 @primary 注解这里就会true
     */
    @Getter
    @Setter
    private boolean primary = false;

    /**
     * 当前 bean作为自动注入的候选人
     */
    @Getter
    @Setter
    private boolean autowireCandidate = true;


    @Getter
    @Setter
    private String factoryBeanName;

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
    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
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
    public String getResourceDescription() {
        return (this.resource != null ? this.resource.getDescription() : null);
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
        return !getConstructorArgumentValues().isEmpty();
    }

    @Override
    public ConstructorArgumentValues getConstructorArgumentValues() {
        if (this.constructorArgumentValues == null) {
            this.constructorArgumentValues = new ConstructorArgumentValues();
        }
        return this.constructorArgumentValues;
    }
}
