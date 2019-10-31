package chy.test.service;


import com.chy.summer.framework.annotation.stereotype.Service;
import com.chy.summer.framework.context.annotation.Lazy;

@Service("abc2222222")
@Lazy
public class Abc2 {
    public void test(){
        System.out.println("------------> 打印了呀");
    }
}



