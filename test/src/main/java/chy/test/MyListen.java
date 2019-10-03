package chy.test;

import chy.test.event.MyEvent;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.context.event.ApplicationListener;
import com.chy.summer.framework.context.event.SimpleApplicationEventMulticaster;
import com.chy.summer.framework.core.ResolvableType;

public class MyListen implements ApplicationListener<MyEvent> {


    @Override
    public void onApplicationEvent(MyEvent event) {
        System.out.println("触发了监听事件了哦");
        System.out.println(event.getSource());
    }


    public static void main(String[] args) {
        SimpleApplicationEventMulticaster s  = new SimpleApplicationEventMulticaster(new DefaultListableBeanFactory());
        s.addApplicationListener(new MyListen());
        s.multicastEvent(new MyEvent("哈哈哈哈哈第一个事件触发"));
    }

}
