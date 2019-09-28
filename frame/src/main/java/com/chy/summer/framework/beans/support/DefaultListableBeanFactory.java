package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DefaultListableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(256);

    /**
     * 用来存放所有注册进来的 beanDefinition 的名字
     */
    private List<String> beanDefinitionNames = new CopyOnWriteArrayList<String>();

    /**
     * 是否允许注册 bean描述的时候覆盖同名的
     */
    private boolean allowBeanDefinitionOverriding = true;

    @Override
    public Object getBean(String name) {
        return null;
    }

    @Override
    public void ignoreDependencyType(Class<?> type) {

    }

    @Override
    public void ignoreDependencyInterface(Class<?> ifc) {

    }

    //======================BeanDefinitionRegistry 的实现方法=================================

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return false;
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return null;
    }

    /**
     * 把 beanDefinition 注册进 容器
     * @param beanName
     * @param beanDefinition
     * @throws BeanDefinitionStoreException
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {

        //检查一波参数
        Assert.hasText(beanName, "Bean name must not be empty");
        Assert.notNull(beanDefinition, "BeanDefinition must not be null");

        BeanDefinition oldBeanDefinition = this.beanDefinitionMap.get(beanName);
        if(oldBeanDefinition != null){
            //有同名的BeanDefinition 走覆盖逻辑
            beanDefinitionOverridingHandle(oldBeanDefinition,beanName,beanDefinition);
        }


    }

    /**
     * 如果已经存在了对应name的 beanDefinition 存在容器中，那么这个方法登场
     */
    private void beanDefinitionOverridingHandle(BeanDefinition oldBeanDefinition, String beanName,
                                                BeanDefinition beanDefinition) {
        //容器默认不给覆盖的话，直接异常
        if(!allowBeanDefinitionOverriding){
            String es = String.format("Cannot register bean definition [%s] for bean %s There is already [%s] bound"
                    ,beanDefinition,beanName,oldBeanDefinition);
            throw new BeanDefinitionStoreException(es);
        }


    }
}
