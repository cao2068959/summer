package com.chy.summer.framework.core.evn.resolver;


import com.chy.summer.framework.core.evn.propertysource.PropertySource;
import com.chy.summer.framework.core.evn.propertysource.PropertySources;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertySourcesPropertyResolver extends AbstractPropertyResolver {


    private final PropertySources propertySources;

    public PropertySourcesPropertyResolver(PropertySources propertySources) {
        this.propertySources = propertySources;
    }

    @Override
    public boolean containsProperty(String key) {
        if (this.propertySources != null) {
            for (PropertySource<?> propertySource : this.propertySources) {
                if (propertySource.containsProperty(key)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected String getPropertyAsRawString(String key) {
        return  doGetProperty(key, String.class, false);
    }

    @Override
    public String getProperty(String key) {
        return doGetProperty(key, String.class, true);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return doGetProperty(key, targetType, true);
    }


    protected <T> T doGetProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
        if (this.propertySources != null) {
            for (PropertySource<?> propertySource : this.propertySources) {
                log.trace("搜索key : [{}] 在 propertysource: [{}] 中", key, propertySource.getName());

                Object value = propertySource.getProperty(key);
                if (value != null) {
                    //是否需要去解析 特殊的表达式 比如 ${xxx:124} 这样的
                    if (resolveNestedPlaceholders && value instanceof String) {
                        //去解析表达式
                        value = resolveNestedPlaceholders((String) value);
                    }
                    log.debug("找到 对应key [{}] 在 [{}] 里面,对应的值是 [{}]", key, propertySource.getName(), value);
                    //因为上面解析出来的都是 string类型,这里根据类型做对应的转换
                    return convertValueIfNecessary(value, targetValueType);
                }
            }
        }
        log.trace("没有找到key : [{}] 随对应的值", key);
        return null;
    }

}
