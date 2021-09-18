package com.chy.summer.framework.boot.autoconfigure.condition;

import com.chy.summer.framework.boot.autoconfigure.AutoConfigurationMetadata;
import com.chy.summer.framework.boot.autoconfigure.FilteringSpringBootCondition;

import java.util.Set;


public class OnBeanCondition extends FilteringSpringBootCondition {


    @Override
    protected final ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses,
                                                   AutoConfigurationMetadata autoConfigurationMetadata) {
        ConditionOutcome[] outcomes = new ConditionOutcome[autoConfigurationClasses.length];
        //TODO 展示不处理
        return outcomes;
    }
}
