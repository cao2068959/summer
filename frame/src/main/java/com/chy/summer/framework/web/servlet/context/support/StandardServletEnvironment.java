package com.chy.summer.framework.web.servlet.context.support;

import com.chy.summer.framework.core.evn.ConfigurableEnvironment;
import com.chy.summer.framework.web.servlet.context.ConfigurableWebEnvironment;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Map;

public class StandardServletEnvironment implements ConfigurableWebEnvironment {

    @Override
    public void initPropertySources(ServletContext servletContext, ServletConfig servletConfig) {

    }

    @Override
    public void setActiveProfiles(String... profiles) {

    }

    @Override
    public void addActiveProfile(String profile) {

    }

    @Override
    public void setDefaultProfiles(String... profiles) {

    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        return null;
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        return null;
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {

    }

    @Override
    public String[] getActiveProfiles() {
        return new String[0];
    }

    @Override
    public String[] getDefaultProfiles() {
        return new String[0];
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return false;
    }

    @Override
    public boolean containsProperty(String key) {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return null;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return null;
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return null;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return null;
    }

    @Override
    public String resolvePlaceholders(String text) {
        return null;
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return null;
    }
}
