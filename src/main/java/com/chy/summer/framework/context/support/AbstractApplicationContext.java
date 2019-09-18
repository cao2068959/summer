package com.chy.summer.framework.context.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractApplicationContext   implements ApplicationContext {

    /**
     * 用来refresh 的时候上锁
     */
    private final Object startupShutdownMonitor = new Object();

    /** 容器刷新的开始时间 */
    private long startupDate;

    /** 容器是否开始活动的一个状态 */
    private final AtomicBoolean active = new AtomicBoolean();

    /** 容器是否关闭的一个状态 */
    private final AtomicBoolean closed = new AtomicBoolean();


    protected void refresh(){
        synchronized (startupShutdownMonitor){
            //容器刷新之前的准备，记录一下活动状态，以及容器开始刷新的时间
            prepareRefresh();

            // 生成对应的 beanFactory 工厂
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // beanFactory 工厂 的准备工作，设置一些属性
            prepareBeanFactory(beanFactory);

            //beanFactory 初始化结束后做一些事情，在spring里面这是个扩展点。
            //这里开始扫描包路径下的类，把标记了注解的玩意生成 beanDefintion
            postProcessBeanFactory(beanFactory);

//            // Invoke factory processors registered as beans in the context.
//            invokeBeanFactoryPostProcessors(beanFactory);
//
//            // Register bean processors that intercept bean creation.
//            registerBeanPostProcessors(beanFactory);
//
//            // Initialize message source for this context.
//            initMessageSource();
//
//            // Initialize event multicaster for this context.
//            initApplicationEventMulticaster();
//
//            // Initialize other special beans in specific context subclasses.
//            onRefresh();
//
//            // Check for listener beans and register them.
//            registerListeners();
//
//            // Instantiate all remaining (non-lazy-init) singletons.
//            finishBeanFactoryInitialization(beanFactory);
//
//            // Last step: publish corresponding event.
//            finishRefresh();
        }
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
    }

//==========================GenericApplicationContext 来实现的模板方法==========================

    public abstract ConfigurableListableBeanFactory getBeanFactory();

    public abstract void freshBeanFactory();
//==========================AnnotationConfigServletWebServerApplicationContext 来实现的模板方法==========================
    public abstract void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory);

}
