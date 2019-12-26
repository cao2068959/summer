package chy.test.conditionTest;


import com.chy.summer.framework.annotation.stereotype.Component;
import com.chy.summer.framework.annotation.stereotype.Service;
import com.chy.summer.framework.context.annotation.condition.Conditional;

@Service
@Conditional(value=Ca.class)
@ConditionalB
public class ConditionDemo1 {

}
