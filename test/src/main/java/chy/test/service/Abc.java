package chy.test.service;


import chy.test.annotation.ChyService;
import com.chy.summer.framework.context.annotation.Scope;
import com.chy.summer.framework.context.annotation.constant.ScopeType;

@ChyService("啦啦啦啦")
@Scope(value = ScopeType.PROTOTYPE,a="222")
public class Abc {

}
