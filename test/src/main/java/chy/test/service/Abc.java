package chy.test.service;


import chy.test.annotation.ChyService;
import com.chy.summer.framework.annotation.beans.Autowired;
import com.chy.summer.framework.context.annotation.Scope;
import com.chy.summer.framework.context.annotation.constant.ScopeType;

@ChyService("啦啦啦啦")
public class Abc {

    @Autowired
    public  Abc2 abc2;

    public void test(){
        abc2.test();
    }

}
