package chy.test.service;


import com.chy.summer.framework.annotation.beans.Autowired;
import com.chy.summer.framework.annotation.stereotype.Service;
import com.chy.summer.framework.context.annotation.Lazy;
import org.checkerframework.checker.units.qual.A;

@Service("abc2222222")
@Lazy
public class Abc2 {

    @Autowired
    Abc abc;

    public void test(){
        System.out.println("循环依赖测试 执行了 Abc2 里面的方法呀呀呀有");
    }

    public void test2(){
        abc.test2();
    }
}



