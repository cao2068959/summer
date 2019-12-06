package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.client.WebClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * web客户端的一个内部使用接口，用于添加拦截器对请求或者响应进行处理
 */
public interface WebClientInternal extends WebClient {

    <T> HttpContext<T> createContext(Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 在链中添加拦截器
     * <p>
     * 这些拦截器可以通过{@link HttpContext#get(String)}/{@link HttpContext#set(String, Object)}方法对每一个请求的状态进行维护
     * 一个请求或者响应可能会在重试的情况下被多次处理，所有应该使用状态标识来保证一次操作不会被执行两次
     * <p>
     * 这个接口只会在内部使用
     *
     * @param interceptor 要添加的拦截器，不可为空
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientInternal addInterceptor(Handler<HttpContext<?>> interceptor);

}