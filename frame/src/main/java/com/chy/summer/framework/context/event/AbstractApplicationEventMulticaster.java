package com.chy.summer.framework.context.event;

import com.chy.summer.framework.aop.framework.AopProxyUtils;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster {

    /**
     * 一个正真保存 事件监听器的 内部类容器
     */
    private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

    private BeanFactory beanFactory;

    public AbstractApplicationEventMulticaster(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
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

        //获取 事件源
        Object source = event.getSource();
        Class<?> sourceType = (source != null ? source.getClass() : null);
        //缓存 事件的类型+事件来源的类型 来决定key
        //ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
        //ListenerRetriever retriever = this.retrieverCache.get(cacheKey);

        if (retriever != null) {
            return retriever.getApplicationListeners();
        }

        return retrieveApplicationListeners(eventType, sourceType, null);

    }


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
            if (supportsEvent(listener, eventType, sourceType)) {
                if (retriever != null) {
                    retriever.applicationListeners.add(listener);
                }
                allListeners.add(listener);
            }
        }
        if (!listenerBeans.isEmpty()) {
            BeanFactory beanFactory = getBeanFactory();
            for (String listenerBeanName : listenerBeans) {
                try {
                    Class<?> listenerType = beanFactory.getType(listenerBeanName);
                    if (listenerType == null || supportsEvent(listenerType, eventType)) {
                        ApplicationListener<?> listener =
                                beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                        if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
                            if (retriever != null) {
                                retriever.applicationListenerBeans.add(listenerBeanName);
                            }
                            allListeners.add(listener);
                        }
                    }
                }
                catch (NoSuchBeanDefinitionException ex) {
                    // Singleton listener instance (without backing bean definition) disappeared -
                    // probably in the middle of the destruction phase
                }
            }
        }
        AnnotationAwareOrderComparator.sort(allListeners);
        return allListeners;
    }

    protected boolean supportsEvent(
            ApplicationListener<?> listener, ResolvableType eventType, @Nullable Class<?> sourceType) {

        GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
                (GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
        return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
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


}
