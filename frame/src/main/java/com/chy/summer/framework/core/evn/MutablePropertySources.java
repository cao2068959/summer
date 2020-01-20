package com.chy.summer.framework.core.evn;


import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 这就是 PropertySource 的一个容器，可以在这个类里按照一定顺序去把PropertySource 插入容器里
 */
public class MutablePropertySources implements PropertySources {

    private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<>();

    public MutablePropertySources() {
    }


    public MutablePropertySources(PropertySources propertySources) {
        this();
        for (PropertySource<?> propertySource : propertySources) {
            addLast(propertySource);
        }
    }


    @Override
    public boolean contains(String name) {
        if (getIndex(name) == -1) {
            return false;
        }
        return true;
    }

    @Override
    public PropertySource<?> get(String name) {
        int index = getIndex(name);
        return index == -1 ? null : propertySourceList.get(index);
    }

    @NotNull
    @Override
    public Iterator<PropertySource<?>> iterator() {
        return propertySourceList.iterator();
    }

    /**
     * 添加配置文件到最后的位置
     *
     * @param propertySource
     */
    public void addLast(PropertySource<?> propertySource) {
        //如果已经存在相同名称的配置文件，那么就先删除原来的
        removeIfPresent(propertySource);
        this.propertySourceList.add(propertySource);
    }

    /**
     * 添加的时候从头开始插
     *
     * @param propertySource
     */
    public void addFirst(PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySourceList.add(0, propertySource);
    }


    /**
     * 添加到某个配置文件的前面
     */
    public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        removeIfPresent(propertySource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index, propertySource);
    }

    /**
     * 添加到某个配置文件的后面
     */
    public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        removeIfPresent(propertySource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index + 1, propertySource);
    }


    /**
     * 删除
     *
     * @param propertySource
     */
    protected void removeIfPresent(PropertySource<?> propertySource) {
        this.propertySourceList.remove(propertySource);
    }

    /**
     * 判断要插入的 properSource 是不是就是他自己，如果是就直接报错了
     *
     * @param relativePropertySourceName
     * @param propertySource
     */
    protected void assertLegalRelativeAddition(String relativePropertySourceName, PropertySource<?> propertySource) {
        String newPropertySourceName = propertySource.getName();
        if (relativePropertySourceName.equals(newPropertySourceName)) {
            throw new IllegalArgumentException(
                    "PropertySource named '" + newPropertySourceName + "' cannot be added relative to itself");
        }
    }

    /**
     * 查找 指定name的配置文件当前排在的位置
     *
     * @param name
     * @return
     */
    private int assertPresentAndGetIndex(String name) {
        int index = getIndex(name);
        if (index == -1) {
            throw new IllegalArgumentException("PropertySource named '" + name + "' does not exist");
        }
        return index;
    }

    private int getIndex(String name) {
        Integer index = 0;
        for (PropertySource<?> propertySource : propertySourceList) {
            if (propertySource.name.equals(name)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private void addAtIndex(int index, PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySourceList.add(index, propertySource);
    }


}
