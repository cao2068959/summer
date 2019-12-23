package com.chy.summer.link.api;

import com.chy.summer.link.api.impl.RequestParameterImpl;
import com.sun.istack.internal.Nullable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 请求参数的持有人接口
 */
public interface RequestParameter {

    /**
     * 设置参数名称
     *
     * @param name
     */
    void setName(String name);

    /**
     * 设置值
     *
     * @param value
     */
    void setValue(Object value);

    /**
     * 获取参数名
     *
     * @return
     */
    @Nullable
    String getName();

    /**
     * 如果参数值是一个map，那么就返回map的key，否则返回null
     *
     * @return
     */
    @Nullable
    List<String> getObjectKeys();

    /**
     * 如果参数值是一个map，那么就返回输入的key对应的value，否则返回null
     *
     * @param key
     */
    @Nullable
    RequestParameter getObjectValue(String key);

    /**
     * 如果请求参数是一个map字段，则返回true
     */
    boolean isObject();

    /**
     * 如果请求参数是一个list，则返回值，否则返回null
     */
    @Nullable
    List<RequestParameter> getArray();

    /**
     * 如果请求参数是一个list，则返回true，否则返回value
     */
    boolean isArray();

    /**
     * 如果请求参数是一个String，则返回值，否则返回null
     */
    @Nullable
    String getString();

    /**
     * 如果请求参数是一个String，则返回true，否则返回value
     */
    boolean isString();

    /**
     * 如果请求参数是一个Integer，则返回值，否则返回null
     */
    @Nullable
    Integer getInteger();

    /**
     * 如果请求参数是一个Integer，则返回true，否则返回value
     */
    boolean isInteger();

    /**
     * 如果请求参数是一个Long，则返回值，否则返回null
     */
    @Nullable
    Long getLong();

    /**
     * 如果请求参数是一个Long，则返回true，否则返回value
     */
    boolean isLong();

    /**
     * 如果请求参数是一个Float，则返回值，否则返回null
     */
    @Nullable
    Float getFloat();

    /**
     * 如果请求参数是一个Float，则返回true，否则返回value
     */
    boolean isFloat();

    /**
     * 如果请求参数是一个Double，则返回值，否则返回null
     */
    @Nullable
    Double getDouble();

    /**
     * 如果请求参数是一个Double，则返回true，否则返回value
     */
    boolean isDouble();

    /**
     * 如果请求参数是一个Boolean，则返回值，否则返回null
     */
    @Nullable
    Boolean getBoolean();

    /**
     * 如果请求参数是一个Boolean，则返回true，否则返回value
     *
     * @return
     */
    boolean isBoolean();

    /**
     * 如果请求参数是一个JsonObject，则返回值，否则返回null
     */
    @Nullable
    JsonObject getJsonObject();

    /**
     * 如果请求参数是一个JsonObject，则返回true，否则返回value
     */
    boolean isJsonObject();

    /**
     * 如果请求参数是一个JsonArray，则返回值，否则返回null
     */
    @Nullable
    JsonArray getJsonArray();

    /**
     * 如果请求参数是一个JsonArray，则返回true，否则返回value
     */
    boolean isJsonArray();

    /**
     * 返回值是否为null
     */
    boolean isNull();

    /**
     * 和isNull一样
     */
    boolean isEmpty();

    /**
     * 将这个请求参数转化成json形式来表示
     *
     * @return
     */
    Object toJson();

    /**
     * 将此请求参数与另一个参数合并
     * 重复参数以传入的参数为准
     */
    RequestParameter merge(RequestParameter otherParameter);

    static RequestParameter create(String name, Object value) {
        return new RequestParameterImpl(name, value);
    }

    static RequestParameter create(Object value) {
        return new RequestParameterImpl(null, value);
    }

}