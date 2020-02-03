package com.chy.summer.framework.boot.context.event;

import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.context.event.ApplicationEvent;
import lombok.Getter;


/**
 * summer 的事件对象, 如果要自定义新的事件,继承这个类
 * 对比 ApplicationEvent , SummerApplicationEvent 统一传递了 容器的对象 context 等公用信息
 *
 */
public abstract class SummerApplicationEvent extends ApplicationEvent {


    @Getter
    private final String[] args;

    public SummerApplicationEvent(SummerApplication application, String[] args) {
        //设置这个事件的来源
        super(application);
        this.args = args;
    }

    /**
     * 获取 application 容器
     * @return
     */
    public SummerApplication getSummerApplication(){
        return (SummerApplication) getSource();
    }

}
