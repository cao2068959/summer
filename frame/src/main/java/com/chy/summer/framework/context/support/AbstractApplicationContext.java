package com.chy.summer.framework.context.support;

import com.chy.summer.framework.beans.config.BeanFactoryPostProcessor;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.event.ApplicationEvent;
import com.chy.summer.framework.context.event.ApplicationEventMulticaster;
import com.chy.summer.framework.context.event.ApplicationListener;
import com.chy.summer.framework.context.event.SimpleApplicationEventMulticaster;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用程序的上下文
 * 实现了通用的方法，总管bean的生命周期和bean对象的资源获取，但不实现资源获取和方法的细节
 */
@Slf4j
public abstract class AbstractApplicationContext implements ApplicationContext {

    /**
     * 用来refresh 的时候上锁
     */
    private final Object startupShutdownMonitor = new Object();

    /**
     * 容器刷新的开始时间
     */
    private long startupDate;

    /**
     * 容器是否开始活动的一个状态
     */
    private final AtomicBoolean active = new AtomicBoolean();

    /**
     * 容器是否关闭的一个状态
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     *  beanFactory 后置处理器的列表,这里传入的优先级比 直接注册到ioc容器里的 后置处理器
     */
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

    /**
     * 监听器的列表
     */
    private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

    /**
     * 早期的事件列表
     */
    private Set<ApplicationEvent> earlyApplicationEvents;

    public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";



    /**
     * 事件广播器
     */
    private ApplicationEventMulticaster applicationEventMulticaster;


    /**
     * 开启summer的生命周期
     */
    protected void refresh() {
        synchronized (startupShutdownMonitor) {
            //容器刷新之前的准备，记录一下活动状态，以及容器开始刷新的时间
            prepareRefresh();

            // 生成对应的 beanFactory 工厂
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // beanFactory 工厂 的准备工作，设置一些属性
            prepareBeanFactory(beanFactory);

            try {
                //beanFactory 初始化结束后做一些事情，在spring里面这是个扩展点。
                //这里开始扫描包路径下的类，把标记了注解的玩意生成 beanDefintion
                //这里要传了包路径才会真正的扫描，如果是spring boot这样的启动则不会在这里去扫描
                postProcessBeanFactory(beanFactory);

                //执行了bean工厂的后置处理器
                invokeBeanFactoryPostProcessors(beanFactory);

                // 注册 bean 的后置处理器 BeanPostProcessor 的接口的bean会被注册进去
                registerBeanPostProcessors(beanFactory);

                // 国际化
                initMessageSource();

                // 初始化事件广播器
                initApplicationEventMulticaster();

                //这里是一个扩展点,如果是初始化 WebApplicationContext 的一些构建就从这里开始
                //明显也是启动tomcat的站点
                onRefresh();

                //检查监听bean 然后注册监听器
                registerListeners();

//            // Instantiate all remaining (non-lazy-init) singletons.
//            finishBeanFactoryInitialization(beanFactory);
//
//            // Last step: publish corresponding event.
//            finishRefresh();


            }catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }



        }
    }

    private void registerListeners() {

        //把手动添加的事件 全部给注册进入到 事件广播器里面
        for (ApplicationListener<?> listener : getApplicationListeners()) {
            getApplicationEventMulticaster().addApplicationListener(listener);
        }

        //去ioc 容器里找到所有 继承了 接口ApplicationListener 的bean,然后也注册进去
        String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
        for (String listenerBeanName : listenerBeanNames) {
            getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
        }

        // 把前提设置的事件 也放入事件广播器,同时清空所有的 前提事件¡
        Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
        this.earlyApplicationEvents = null;
        if (earlyEventsToProcess != null) {
            for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
                getApplicationEventMulticaster().multicastEvent(earlyEvent);
            }
        }
    }

    public Collection<ApplicationListener<?>> getApplicationListeners() {
        return this.applicationListeners;
    }

    public ApplicationEventMulticaster getApplicationEventMulticaster(){
        return this.applicationEventMulticaster;
    }


    private void onRefresh() {
    }

    private void initApplicationEventMulticaster() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();

        //如果容器里已经有事件广播器 可以走这里
        //一般 用户可以 注册一个bean 名字叫  applicationEventMulticaster 并且 实现 ApplicationEventMulticaster 接口就可以走这里
        if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
            this.applicationEventMulticaster =
                    beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
            log.debug("使用 自定义的 事件广播器 [%s]",applicationEventMulticaster);
        }
        else {
            //如果容器里没有已经初始化好的事件广播器,就自己创建一个
            //如果是 refresh 方法来的都是走这里
            this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
            beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
            log.debug("使用 默认的 事件广播器 [%s]",applicationEventMulticaster);
        }
    }

    private void initMessageSource() {
    }

    protected void  registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory){
        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory,this);
    }

    private void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        //开始执行后置处理器
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

    }

    private List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return this.beanFactoryPostProcessors;
    }


    private void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        //TODO 用了给beanFactory 设置一些属性，添加一些可能忽略的类什么的
    }

    private ConfigurableListableBeanFactory obtainFreshBeanFactory() {

        freshBeanFactory();
        return getBeanFactory();
    }

    private void prepareRefresh() {
        this.startupDate = System.currentTimeMillis();
        this.closed.set(false);
        this.active.set(true);
        this.earlyApplicationEvents = new LinkedHashSet<>();
    }

    //==========================GenericApplicationContext 来实现的模板方法==========================

    public abstract ConfigurableListableBeanFactory getBeanFactory();

    public abstract void freshBeanFactory();

    //==========================AnnotationConfigServletWebServerApplicationContext 来实现的模板方法==========================
    public abstract void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws IOException;

}
