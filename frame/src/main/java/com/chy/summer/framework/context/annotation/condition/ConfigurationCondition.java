package com.chy.summer.framework.context.annotation.condition;


public interface ConfigurationCondition {


    enum ConfigurationPhase {

        /**
         *  Condition 作用于的是 class文件
         */
        PARSE_CONFIGURATION,

        /**
         *  Condition 作用于方法上面，比如直接 @Condition 注解放在 @Bean 方法上面
         */
        REGISTER_BEAN
    }
}
