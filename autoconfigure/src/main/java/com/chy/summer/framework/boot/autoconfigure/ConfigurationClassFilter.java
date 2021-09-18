package com.chy.summer.framework.boot.autoconfigure;


import com.chy.summer.framework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConfigurationClassFilter {

    /**
     * 从文件 spring-autoconfigure-metadata.properties 中读出来的自动配置缓存，该文件中记录了自动配置类中是否启动的条件
     * 如是否有 @ConditionalOnBean @ConditionalOnClass 如果有那些条件是什么，这么做的目的在于可以不去解析整个 自动配置类的 class文件就能
     * 提前去判断该类是否应该加载，少了一步解析步骤，提高了性能
     *
     * 在使用 autoConfigurationMetadata 缓存去做条件判断的时候，使用了多线程
     *
     */
    private final AutoConfigurationMetadata autoConfigurationMetadata;

    private final List<AutoConfigurationImportFilter> filters;

    ConfigurationClassFilter(ClassLoader classLoader, List<AutoConfigurationImportFilter> filters) {
        //TODO 先留坑，暂时不支持 autoConfigurationMetadata
        //this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(classLoader);
        this.autoConfigurationMetadata = null;
        this.filters = filters;
    }


    List<String> filter(List<String> configurations) {
        long startTime = System.nanoTime();
        String[] candidates = StringUtils.toStringArray(configurations);
        boolean skipped = false;
        for (AutoConfigurationImportFilter filter : this.filters) {
            boolean[] match = filter.match(candidates, this.autoConfigurationMetadata);
            for (int i = 0; i < match.length; i++) {
                if (!match[i]) {
                    candidates[i] = null;
                    skipped = true;
                }
            }
        }
        if (!skipped) {
            return configurations;
        }
        List<String> result = new ArrayList<>(candidates.length);
        for (String candidate : candidates) {
            if (candidate != null) {
                result.add(candidate);
            }
        }
        if (log.isTraceEnabled()) {
            int numberFiltered = configurations.size() - result.size();
            log.trace("Filtered " + numberFiltered + " auto configuration class in "
                    + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
        }
        return result;
    }

}