package com.chy.summer.framework.boot.autoconfigure;


import com.chy.summer.framework.boot.autoconfigure.condition.ConditionOutcome;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FilteringSpringBootCondition implements AutoConfigurationImportFilter {


    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        //让子类去实现对应的判断逻辑
        ConditionOutcome[] outcomes = getOutcomes(autoConfigurationClasses, autoConfigurationMetadata);
        boolean[] match = new boolean[outcomes.length];
        for (int i = 0; i < outcomes.length; i++) {
            //如果 值为null, 那么就改成true
            match[i] = (outcomes[i] == null || outcomes[i].isMatch());
            if (!match[i] && outcomes[i] != null) {
                log.trace("class [" + autoConfigurationClasses[i] + "] 没有满足加载条件 原因: [" + outcomes[i].getMessage() + "]");
            }
        }
        return match;
    }

    protected abstract ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata);


}
