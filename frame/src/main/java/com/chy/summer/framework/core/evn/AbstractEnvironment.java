package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.core.evn.propertysource.MutablePropertySources;
import com.chy.summer.framework.core.evn.resolver.ConfigurablePropertyResolver;
import com.chy.summer.framework.core.evn.resolver.PropertySourcesPropertyResolver;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.AccessControlException;
import java.util.*;

/**
 *  environment 的抽象类，大量解析配置文件的方法就在这里面
 */
@Slf4j
public abstract class AbstractEnvironment implements ConfigurableEnvironment  {

    /**
     * 用来区别不同的环境，的配置项，其实也就是这个属性决定了去激活那些配置项
     */
    public static final String ACTIVE_PROFILES_PROPERTY_NAME = "summer.profiles.active";

    public static final String DEFAULT_PROFILES_PROPERTY_NAME = "summer.profiles.default";

    protected static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

    /**
     * 放了当前需要 活跃的配置文件
     */
    private final Set<String> activeProfiles = new LinkedHashSet<>();

    private final Set<String> defaultProfiles = new LinkedHashSet<>(getReservedDefaultProfiles());

    /**
     * 真正存放配置属性的容器
     */
    @Getter
    private final MutablePropertySources propertySources = new MutablePropertySources();

    /**
     * 属性解析器，数据来源就是上面的 propertySources
     */
    private final ConfigurablePropertyResolver propertyResolver =
            new PropertySourcesPropertyResolver(this.propertySources);

    public AbstractEnvironment(){
        customizePropertySources(propertySources);
    }

    /**
     * 用来给子类去重写,会在构造函数中去 初始化一些配置
     */
    protected abstract void customizePropertySources(MutablePropertySources propertySources);

    /**
     * 设置指定配置文件为活跃状态
     */
    @Override
    public void setActiveProfiles(String... profiles) {
        log.debug("激活配置文件 {}",Arrays.asList(profiles));
        synchronized (this.activeProfiles) {
            this.activeProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                this.activeProfiles.add(profile);
            }
        }
    }

    /**
     * 添加配置文件为活跃状态
     * 和上面 setActiveProfiles 的区别在于，这个方法是增量添加的，setActiveProfiles是全覆盖的
     */
    @Override
    public void addActiveProfile(String profile) {
        log.debug("添加活跃的配置文件 {}",profile);
        validateProfile(profile);
        doGetActiveProfiles();
        synchronized (this.activeProfiles) {
            this.activeProfiles.add(profile);
        }
    }

    /**
     * 设置默认的配置
     * @param profiles
     */
    @Override
    public void setDefaultProfiles(String... profiles) {
        Assert.notNull(profiles, "profiles 数组不能为null");
        synchronized (this.defaultProfiles) {
            this.defaultProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                this.defaultProfiles.add(profile);
            }
        }
    }

    /**
     * 获取了操作系统的环境变量
     */
    @Override
    public Map<String, Object> getSystemEnvironment() {
        try {
            return (Map) System.getenv();
        }
        catch (AccessControlException ex) {
            log.warn("获取系统环境变量失败:{}",ex.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 获取一些程序属性变量，这里包括 java -D 添加的自定义参数
     * @return
     */
    @Override
    public Map<String, Object> getSystemProperties() {
        try {
            return (Map) System.getProperties();
        }
        catch (AccessControlException ex) {
            log.warn("获取程序属性变量失败:{}",ex.getMessage());
            return Collections.emptyMap();
        }
    }


    @Override
    public void merge(ConfigurableEnvironment parent) {
        String[] parentActiveProfiles = parent.getActiveProfiles();
        if (!ObjectUtils.isEmpty(parentActiveProfiles)) {
            synchronized (this.activeProfiles) {
                for (String profile : parentActiveProfiles) {
                    this.activeProfiles.add(profile);
                }
            }
        }
        String[] parentDefaultProfiles = parent.getDefaultProfiles();
        if (!ObjectUtils.isEmpty(parentDefaultProfiles)) {
            synchronized (this.defaultProfiles) {
                this.defaultProfiles.remove(RESERVED_DEFAULT_PROFILE_NAME);
                for (String profile : parentDefaultProfiles) {
                    this.defaultProfiles.add(profile);
                }
            }
        }
    }


    @Override
    public String[] getActiveProfiles() {
        return StringUtils.toStringArray(doGetActiveProfiles());
    }

    @Override
    public String[] getDefaultProfiles() {
        return StringUtils.toStringArray(doGetDefaultProfiles());
    }


    //---------------------------------------------------------------------
    // 实现 PropertyResolver interface
    //---------------------------------------------------------------------

    @Override
    public boolean containsProperty(String key) {
        return this.propertyResolver.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        return this.propertyResolver.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.propertyResolver.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return this.propertyResolver.getProperty(key, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return this.propertyResolver.getProperty(key, targetType, defaultValue);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return this.propertyResolver.getRequiredProperty(key);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return this.propertyResolver.getRequiredProperty(key, targetType);
    }

    @Override
    public String resolvePlaceholders(String text) {
        return this.propertyResolver.resolvePlaceholders(text);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return this.propertyResolver.resolveRequiredPlaceholders(text);
    }





    protected Set<String> doGetDefaultProfiles() {
        synchronized (this.defaultProfiles) {
            if (this.defaultProfiles.equals(getReservedDefaultProfiles())) {
                String profiles = getProperty(DEFAULT_PROFILES_PROPERTY_NAME);
                if (StringUtils.hasText(profiles)) {
                    setDefaultProfiles(StringUtils.commaDelimitedListToStringArray(
                            StringUtils.trimAllWhitespace(profiles)));
                }
            }
            return this.defaultProfiles;
        }
    }


    /**
     * 获取已经激活的配置文件，如果是第一次获取，还会去把 summer.profiles.active 配置的值给放进去
     */
    protected Set<String> doGetActiveProfiles() {
        synchronized (this.activeProfiles) {
            if (this.activeProfiles.isEmpty()) {
                String profiles = getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
                if (StringUtils.hasText(profiles)) {
                    setActiveProfiles(StringUtils.commaDelimitedListToStringArray(
                            StringUtils.trimAllWhitespace(profiles)));
                }
            }
            return this.activeProfiles;
        }
    }


    /**
     * 验证配置文件的名称是否合法
     */
    protected void validateProfile(String profile) {
        if (!StringUtils.hasText(profile)) {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must contain text");
        }
        if (profile.charAt(0) == '!') {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must not begin with ! operator");
        }
    }

    protected Set<String> getReservedDefaultProfiles() {
        return Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME);
    }

}
