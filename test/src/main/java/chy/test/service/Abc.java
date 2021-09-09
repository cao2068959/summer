package chy.test.service;


import chy.test.annotation.ChyService;
import com.chy.summer.framework.annotation.beans.Autowired;
import com.chy.summer.framework.annotation.beans.Value;
import com.chy.summer.framework.context.annotation.Scope;
import com.chy.summer.framework.context.annotation.constant.ScopeType;

@ChyService("Abc")
public class Abc {

    @Autowired
    public Abc2 abc2;

    @Value("${hahah}")
    private String xxxx;

    public void test() {
        abc2.test();
    }

    public void test2() {
        System.out.println("循环依赖测试 执行了 拉阿拉啦啦啦啦 的方法");
        System.out.println("配置值：" + xxxx);
    }

}
