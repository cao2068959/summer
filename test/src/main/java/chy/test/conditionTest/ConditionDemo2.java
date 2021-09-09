package chy.test.conditionTest;


import com.chy.summer.framework.annotation.stereotype.Service;
import com.chy.summer.framework.context.annotation.Bean;
import com.chy.summer.framework.context.annotation.condition.Conditional;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

@Service
public class ConditionDemo2 {

    public ConditionDemo2() {
    }

    @Bean
    @ConditionalB
    public Date xxx(){
        System.out.println("-------------> 初始化了 xxx");
        return new Date();
    }

}
