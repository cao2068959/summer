package chy.test.service;

import com.chy.summer.framework.context.annotation.Bean;

public class ImprotExec1 {

    public ImprotExec1() {
        System.out.println("构造了 ImprotExec1");
    }

    @Bean
    public BeanDemo2 beanDemo33(){
        BeanDemo2 beanDemo2 = new BeanDemo2();
        beanDemo2.setName("小红");
        return beanDemo2;
    }


}
