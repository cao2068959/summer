package com.chy.summer.framework.context.annotation.condition;


import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.core.type.AnnotationMetadata;

import com.chy.summer.framework.context.annotation.condition.ConfigurationCondition.ConfigurationPhase;
import com.chy.summer.framework.util.ConfigurationClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
public class ConditionEvaluator {

    public boolean shouldSkip(AnnotatedTypeMetadata metadata, ConfigurationPhase phase) {
        //既然没打 @Conditional 注解 直接放行了
        if (metadata == null || !metadata.hasMetaAnnotation(Conditional.class.getName())) {
            return false;
        }
        //如果没有指定 phase 的就自己去判断 这到底是什么类型
        if (phase == null) {
            if (metadata instanceof AnnotationMetadata &&
                    ConfigurationClassUtils.isConfigurationCandidate((AnnotationMetadata) metadata)) {
                return shouldSkip(metadata, ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION);
            }
            return shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN);
        }

        List<Condition> conditions = new ArrayList<Condition>();
        for (String[] conditionClasses : getConditionClasses(metadata)) {
            */
/*for (String conditionClass : conditionClasses) {
                Condition condition = getCondition(conditionClass, this.context.getClassLoader());
                conditions.add(condition);
            }*//*

        }

        */
/*//*
/排序
        AnnotationAwareOrderComparator.sort(conditions);

        for (Condition condition : conditions) {
            ConfigurationPhase requiredPhase = null;
            if (condition instanceof ConfigurationCondition) {
                requiredPhase = ((ConfigurationCondition) condition).getConfigurationPhase();
            }
            if (requiredPhase == null || requiredPhase == phase) {
                if (!condition.matches(this.context, metadata)) {
                    return true;
                }
            }
        }*//*


        return false;
    }


    private List<String[]> getConditionClasses(AnnotatedTypeMetadata metadata) {
        Map<String, AnnotationAttributes> annotationAttributesAll = metadata.getAnnotationAttributesAll(Conditional.class);
        return null;
    }


}
*/
