package com.chy.summer.framework.context.event;

import com.chy.summer.framework.aop.framework.AopProxyUtils;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster {

    /**
     * 一个正真保存 事件监听器的 内部类容器
     */
    private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

    final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);


    private BeanFactory beanFactory;

    public AbstractApplicationEventMulticaster(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    public AbstractApplicationEventMulticaster() {
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {

    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {

    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {

    }

    @Override
    public void removeAllListeners() {

    }

    /**
     * 添加监听
     * @param listener
     */
    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        synchronized (defaultRetriever){
            Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
            if (singletonTarget instanceof ApplicationListener) {
                this.defaultRetriever.applicationListeners.remove(singletonTarget);
            }
            this.defaultRetriever.applicationListeners.add(listener);
        }
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    /**
     * 通过事件找到对应的监听器
     * @param event
     * @param eventType
     * @return
     */
    protected Collection<ApplicationListener<?>> getApplicationListeners(
            ApplicationEvent event, ResolvableType eventType) {

        //获取 事件的来源
        Object source = event.getSource();
        Class<?> sourceType = (source != null ? source.getClass() : null);
        //缓存 事件的类型+事件来源的类型 来决定key

        ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

        ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
        if (retriever != null) {
            return retriever.getApplicationListeners();
        }

        return retrieveApplicationListeners(eventType, sourceType, null);
    }


    /**
     * 用事件把对应的监听器给找出来
     * @param eventType
     * @param sourceType
     * @param retriever
     * @return
     */
    private Collection<ApplicationListener<?>> retrieveApplicationListeners(
            ResolvableType eventType, @Nullable Class<?> sourceType, @Nullable ListenerRetriever retriever) {

        List<ApplicationListener<?>> allListeners = new ArrayList<>();
        Set<ApplicationListener<?>> listeners;
        Set<String> listenerBeans;
        //保证 applicationListeners和applicationListenerBeans 是统一的
        synchronized (this.defaultRetriever) {
            listeners = new LinkedHashSet<>(this.defaultRetriever.applicationListeners);
            listenerBeans = new LinkedHashSet<>(this.defaultRetriever.applicationListenerBeans);
        }

        for (ApplicationListener<?> listener : listeners) {
            //从事件类型,事件来源类上 上判断是不是属于对应的监听器
            if (supportsEvent(listener, eventType, sourceType)) {
                if (retriever != null) {
                    retriever.applicationListeners.add(listener);
                }
                allListeners.add(listener);
            }
        }
        //下面是处理 已经到了 IOC 里用户自定义的 监听器
        if (!listenerBeans.isEmpty()) {
            BeanFactory beanFactory = getBeanFactory();
            for (String listenerBeanName : listenerBeans) {
                try {
                    //拿到已经到了 ioc 容器里的监听器的类型
                    Class<?> listenerType = beanFactory.getType(listenerBeanName);
                    // 额 这里判断一下
                    if (listenerType == null || supportsEvent(listenerType, eventType)) {
                        ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                        //如果 这个监听器还没被注册,并且和对应的事件类型匹配上
                        if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
                            if (retriever != null) {
                                retriever.applicationListenerBeans.add(listenerBeanName);
                            }
                            allListeners.add(listener);
                        }
                    }
                }
                catch (NoSuchBeanDefinitionException ex) {
                    log.debug("[%s] 事件 在查找对应监听器的时候发生异常: %s",ex.getMessage());
                }
            }
        }
        //排序
        AnnotationAwareOrderComparator.sort(allListeners);
        return allListeners;
    }

    protected boolean supportsEvent(Class<?> listenerType, ResolvableType eventType) {
        if (GenericApplicationListener.class.isAssignableFrom(listenerType)) {
            return true;
        }
        //获取了 ApplicationListener 里面的泛型的类型
        ResolvableType declaredEventType = GenericApplicationListenerAdapter.resolveDeclaredEventType(listenerType);
        return (declaredEventType == null || declaredEventType.isAssignableFrom(eventType));
    }

    protected boolean supportsEvent(
            ApplicationListener<?> listener, ResolvableType eventType, @Nullable Class<?> sourceType) {

        GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
                (GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));

        //先判断 事件的类型是否相同
        if(!(smartListener.supportsEventType(eventType))){
            return false;
        }

        //判断 事件的来源是否相同
        if(!(smartListener.supportsSourceType(sourceType))){
            return false;
        }

        return true;

    }


    private class ListenerRetriever {

        public final Set<ApplicationListener<?>> applicationListeners;

        public final Set<String> applicationListenerBeans;

        private final boolean preFiltered;

        public ListenerRetriever(boolean preFiltered) {
            this.applicationListeners = new LinkedHashSet<>();
            this.applicationListenerBeans = new LinkedHashSet<>();
            this.preFiltered = preFiltered;
        }

        /**
         * 获取 所有的 监听器,除了上面 applicationListeners保存下来的,还有以已经注册进入ioc容器的,然后做了去重处理
         * @return
         */
        public Collection<ApplicationListener<?>> getApplicationListeners() {
            List<ApplicationListener<?>> allListeners = new ArrayList<>(
                    this.applicationListeners.size() + this.applicationListenerBeans.size());
            allListeners.addAll(this.applicationListeners);
            if (!this.applicationListenerBeans.isEmpty()) {
                BeanFactory beanFactory = getBeanFactory();
                for (String listenerBeanName : this.applicationListenerBeans) {
                    try {
                        ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                        if (this.preFiltered || !allListeners.contains(listener)) {
                            allListeners.add(listener);
                        }
                    }
                    catch (NoSuchBeanDefinitionException ex) {
                        log.warn("getApplicationListeners 发生 NoSuchBeanDefinitionException异常 : [%s] 获取失败",listenerBeanName);
                    }
                }
            }
            //排序
            AnnotationAwareOrderComparator.sort(allListeners);
            return allListeners;
        }
    }

    /**
     * 缓存类
     */
    private static final class ListenerCacheKey implements Comparable<ListenerCacheKey> {

        private final ResolvableType eventType;

        @Nullable
        private final Class<?> sourceType;

        public ListenerCacheKey(ResolvableType eventType, @Nullable Class<?> sourceType) {
            Assert.notNull(eventType, "事件类型不能为空");
            this.eventType = eventType;
            this.sourceType = sourceType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            ListenerCacheKey otherKey = (ListenerCacheKey) other;
            return (this.eventType.equals(otherKey.eventType) &&
                    ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType));
        }

        @Override
        public int hashCode() {
            return this.eventType.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.sourceType);
        }

        @Override
        public String toString() {
            return "ListenerCacheKey [eventType = " + this.eventType + ", sourceType = " + this.sourceType + "]";
        }

        @Override
        public int compareTo(ListenerCacheKey other) {
            int result = this.eventType.toString().compareTo(other.eventType.toString());
            if (result == 0) {
                if (this.sourceType == null) {
                    return (other.sourceType == null ? 0 : -1);
                }
                if (other.sourceType == null) {
                    return 1;
                }
                result = this.sourceType.getName().compareTo(other.sourceType.getName());
            }
            return result;
        }
    }


}
