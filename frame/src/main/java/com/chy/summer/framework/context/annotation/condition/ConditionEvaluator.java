package com.chy.summer.framework.context.annotation.condition;


import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.core.type.AnnotationBehavior;
import com.chy.summer.framework.core.type.AnnotationMetadata;

import com.chy.summer.framework.context.annotation.condition.ConfigurationCondition.ConfigurationPhase;
import com.chy.summer.framework.util.ConfigurationClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ConditionEvaluator {

    public boolean shouldSkip(AnnotationBehavior annotationBehavior, ConfigurationPhase phase) {
        //既然没打 @Conditional 注解 直接放行了
        if (annotationBehavior == null || !annotationBehavior.hasMetaAnnotation(Conditional.class.getName())) {
            return false;
        }
        //如果没有指定 phase 的就自己去判断 这到底是什么类型
        if (phase == null) {
            if (annotationBehavior instanceof AnnotationMetadata &&
                    ConfigurationClassUtils.isConfigurationCandidate((AnnotationMetadata) annotationBehavior)) {
                return shouldSkip(annotationBehavior, ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION);
            }
            return shouldSkip(annotationBehavior, ConfigurationPhase.REGISTER_BEAN);
        }

        List<Condition> conditions = new ArrayList<Condition>();
        //获取了 @Conditional 注解的 value[], value在传入的时候应该是 Class 类型,这边把他换成 String类型,指代的是class的全路径
        //这里会把 这个 类/方法 上所有的 @Conditional 的 value[] 都给找出来,所以是一个 string[注解数][每个注解上的所有value值] 2维数组
        for (String[] conditionClasses : getConditionClasses(annotationBehavior)) {
            for (String conditionClass : conditionClasses) {
                Condition condition = getCondition(conditionClass, this.context.getClassLoader());
                conditions.add(condition);
            }
        }


        //排序
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
        }

        return false;
    }


    private List<String[]> getConditionClasses(AnnotationBehavior annotationBehavior) {
        return annotationBehavior.getAnnotationAttributesAll(Conditional.class).stream()
                .map(annotationAttributes ->
                        annotationAttributes.getRequiredAttribute("value", String[].class))
                .collect(Collectors.toList());
    }
}