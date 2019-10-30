package com.chy.summer.framework.beans.factory.annotation;


import com.chy.summer.framework.beans.PropertyValues;
import com.chy.summer.framework.beans.config.SmartInstantiationAwareBeanPostProcessor;
import com.chy.summer.framework.beans.support.annotation.InjectionMetadata;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

    /**
     * InjectionMetadata 的缓存
     */
    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {

        return null;
    }

    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        //如果缓存是 Null，或者 metadata 里面的class和我传入的class不同，就需要刷新缓存
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    metadata = buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    /**
     * 用class来构造 InjectionMetadata 对象
     * @param clazz
     * @return
     */
    private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
        return null;
    }
}
