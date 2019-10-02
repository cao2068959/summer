package com.chy.summer.framework.context.event;

import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.util.Assert;

public class GenericApplicationListenerAdapter implements GenericApplicationListener {


    private final ApplicationListener<ApplicationEvent> delegate;

    private final ResolvableType declaredEventType;

    public GenericApplicationListenerAdapter(ApplicationListener<?> delegate) {
        Assert.notNull(delegate, "Delegate listener 不能为Null");
        this.delegate = (ApplicationListener<ApplicationEvent>) delegate;
        this.declaredEventType = resolveDeclaredEventType(this.delegate);
    }

    /**
     * 把一个监听器转成
     * @param listener
     * @return
     */
    private static ResolvableType resolveDeclaredEventType(ApplicationListener<ApplicationEvent> listener) {
        //拿到监听器的泛型
        ResolvableType declaredEventType = resolveDeclaredEventType(listener.getClass());
        //todo 上面如果拿不到,那么 那么 可能是 aop 的代理对象 ,在spring 中还拿了aop中的目标对象去做对比
        return declaredEventType;
    }


    static ResolvableType resolveDeclaredEventType(Class<?> listenerType) {
        //把 监听器的类型包装一下,并且拿到 ApplicationListener 接口的包装实例
        ResolvableType resolvableType = ResolvableType.forClass(listenerType).as(ApplicationListener.class);
        //去获取 ApplicationListener 接口下面的泛型的类型
        return (resolvableType.hasGenerics() ? resolvableType.getGeneric() : null);
    }


    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return false;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
