package com.chy.summer.link.api;

import com.sun.istack.internal.Nullable;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 请求参数的容器
 */
public interface RequestParameters {

    /**
     * 获取请求路径里所有参数的名称，返回一个列表
     */
    List<String> pathParametersNames();

    /**
     * 根据请求参数的名称获取，参数的持有对象
     *
     * @param name 参数名
     */
    @Nullable
    RequestParameter pathParameter(String name);

    /**
     * 获取查询中所有参数名称的列表
     */
    List<String> queryParametersNames();

    /**
     * 根据名称获取查询参数
     *
     * @param name 参数名
     */
    @Nullable
    RequestParameter queryParameter(String name);

    /**
     * 获取请求头里面所有参数名，返回列表
     */
    List<String> headerParametersNames();

    /**
     * 根据参数名获取请求头的参数
     *
     * @param name 参数名
     */
    @Nullable
    RequestParameter headerParameter(String name);

    /**
     * 获取cookie里面所有参数名，返回列表
     */
    List<String> cookieParametersNames();

    /**
     * 根据参数名获取cookie的参数
     *
     * @param name 参数名
     */
    @Nullable
    RequestParameter cookieParameter(String name);

    /**
     * 获取form里面所有参数名，返回列表
     */
    List<String> formParametersNames();

    /**
     * 根据参数名获取form的参数
     *
     * @param name 参数名
     */
    @Nullable
    RequestParameter formParameter(String name);

    /**
     * 返回请求的body
     */
    @Nullable
    RequestParameter body();

    /**
     * 这个方法会将cookie, path, query, header, form转换成JsonObject，
     * 其中键是参数名称，值是参数值，而body将根据格式进行对应的转换
     */
    JsonObject toJson();

}