package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {

    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private BeanFactory parentBeanFactory;


    /**
     * 判断是否已经开始创建 bean 对象了
     * @return
     */
    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

    public abstract Comparator<Object> getDependencyComparator();


    @Override
    public boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        String beanName = BeanFactoryUtils.transformedBeanName(name);
        //获取单列对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                //如果他实现了 FactoryBean 但是又不是 FactoryBean 的bean,那么就直接调用 FactoryBean的 getObjectType 来获取类型
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);
                    //判断typeToMatch 是否是 type 的父类
                    return (type != null && typeToMatch.isAssignableFrom(type));
                }
                else {
                    //判断 typeToMatch 和 实例的关系
                    return typeToMatch.isInstance(beanInstance);
                }
            } else if (!BeanFactoryUtils.isFactoryDereference(name)) {
                if (typeToMatch.isInstance(beanInstance)) {
                    return true;
                }
                //TODO 这里还有泛型的判断,这里先留个坑
            }
            return false;
        }
        else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            return false;
        }

        //TODO 后面还有用 RootBeanDefinition 去判断类型的,这里先留个坑

        return false;
    }

    protected abstract boolean containsBeanDefinition(String beanName);


    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        return factoryBean.getObjectType();
    }


    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "BeanPostProcessor 必须不能为 null");
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public boolean containsLocalBean(String name) {
        String beanName = BeanFactoryUtils.transformedBeanName(name);
        return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
                (!BeanFactoryUtils.isFactoryDereference(name)));
    }

    @Override
    @Nullable
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        String beanName = BeanFactoryUtils.transformedBeanName(name);

        // 尝试拿一下单列对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            //如果拿到了,那么看看是不是 FactoryBean 是的话走 FactoryBean 的专用方法,不是就直接给类型
            if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
            }
            else {
                return beanInstance.getClass();
            }
        }

        // 如果不是单例对象,那么从BeanDefinition 上入手,这里
        //TODO 先留坑
        return null;
    }


    @Nullable
    public BeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }

}
