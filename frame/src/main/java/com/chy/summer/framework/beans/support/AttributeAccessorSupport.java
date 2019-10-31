package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.AttributeAccessor;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  beanDefintion 的属性值处理部分
 */
public abstract class AttributeAccessorSupport implements AttributeAccessor {

    private final Map<String, Object> attributes = new LinkedHashMap<>();


    @Override
    public void setAttribute(String name,  Object value) {
        Assert.notNull(name, "Name 不能为空");
        if (value != null) {
            this.attributes.put(name, value);
        }
        else {
            removeAttribute(name);
        }
    }

    @Override
    public Object getAttribute(String name) {
        Assert.notNull(name, "Name 不能为空");
        return this.attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        Assert.notNull(name, "Name 不能为空");
        return this.attributes.remove(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        Assert.notNull(name, "Name 不能为空");
        return this.attributes.containsKey(name);
    }

    @Override
    public String[] attributeNames() {
        return StringUtils.toStringArray(this.attributes.keySet());
    }



    protected void copyAttributesFrom(AttributeAccessor source) {
        Assert.notNull(source, "Source 不能为空");
        String[] attributeNames = source.attributeNames();
        for (String attributeName : attributeNames) {
            setAttribute(attributeName, source.getAttribute(attributeName));
        }
    }
}
