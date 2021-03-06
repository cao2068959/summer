package com.chy.summer.framework.context.event;

import com.chy.summer.framework.boot.context.event.SmartApplicationListener;
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


    /**
     * 获取 ApplicationListener<T> 这个接口里 的泛型的类型
     * @param listenerType
     * @return
     */
    static ResolvableType resolveDeclaredEventType(Class<?> listenerType) {
        //把 监听器的类型包装一下,并且拿到 ApplicationListener 接口的包装实例
        ResolvableType resolvableType = ResolvableType.forClass(listenerType).as(ApplicationListener.class);
        //去获取 ApplicationListener 接口下面有没泛型
        if(resolvableType.hasGenerics() ){
            //有的话获取第一个位置的泛型类型
            return resolvableType.getGeneric();
        }
        //没有就是null 走人
        return null;
    }


    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        //如果他是 SmartApplicationListener 类型就直接走 SmartApplicationListener 的判断逻辑
        if (this.delegate instanceof SmartApplicationListener) {
            Class<? extends ApplicationEvent> eventClass = (Class<? extends ApplicationEvent>) eventType.resolve();
            if(eventClass == null){
                return false;
            }

            return ((SmartApplicationListener) this.delegate).supportsEventType(eventClass);
        }

        //如果不是 SmartApplicationListener 类型 就去判断泛型是否相同
        return (this.declaredEventType == null || this.declaredEventType.isAssignableFrom(eventType));

    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        //如果他不是SmartApplicationListener 类型就直接放行了
        if(!(this.delegate instanceof SmartApplicationListener)){
            return true;
        }

        //如果是 SmartApplicationListener 类型是直接走 SmartApplicationListener 的验证逻辑
        return ((SmartApplicationListener) this.delegate).supportsSourceType(sourceType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
