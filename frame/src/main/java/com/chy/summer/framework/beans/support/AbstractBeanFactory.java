package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.ConfigurableBeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.HierarchicalBeanFactory;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.chy.summer.framework.util.BeanFactoryUtils.transformedBeanName;

public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements HierarchicalBeanFactory {

    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private BeanFactory parentBeanFactory;

    protected final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

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
        String beanName = transformedBeanName(name);
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

    @Override
    public boolean containsLocalBean(String name) {
        String beanName = transformedBeanName(name);
        return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
                (!BeanFactoryUtils.isFactoryDereference(name)));
    }

    @Override
    @Nullable
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

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


    /**
     * 返回一个合并后的 BeanDefinition,如果对应的 Bean 存在父子关系的话
     * 因为这个项目主要是 已经注解的方式为主导,这边beanDefinition 的父子继承并不是使用太多,这边就先不填这个坑
     * @param beanName
     * @param bd
     * @param containingBd
     * @return
     * @throws BeanDefinitionStoreException
     */
    protected RootBeanDefinition getMergedBeanDefinition(
            String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
            throws BeanDefinitionStoreException {

        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;

            //这里再检查一次,防止上锁之前
            if (containingBd == null) {
                mbd = this.mergedBeanDefinitions.get(beanName);
            }

            if (mbd == null) {
                //先看要合并的 BeanDefinition 有没有父 bd ,如果没有就是直接封装进去 RootBeanDefinition
                if (bd.getParentName() == null) {
                    // 如果已经是 RootBeanDefinition 对象,那么深度拷贝出来
                    if (bd instanceof RootBeanDefinition) {
                        mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                    }
                    else {
                        mbd = new RootBeanDefinition(bd);
                    }
                } else {
                    //TODO 下面开始是 如果是有父 BeanDefinition 的情况下
                }
            }

            return mbd;
        }
    }


    @Override
    public boolean containsBean(String beanName) {
        return false;
    }
}
