package com.chy.summer.link.client;

import com.chy.summer.link.client.impl.WebClientSessionAware;
import com.chy.summer.link.client.spi.CookieStore;

/**
 * 异步会话 HTTP和HTTP/2客户端接口
 */
public interface WebClientSession extends WebClient {

    /**
     * 使用提供的webClient实例创建一个WebClientSession。
     *
     * @param webClient webClient实例
     * @return 创建的客户端
     */
    static WebClientSession create(WebClient webClient) {
        return create(webClient, CookieStore.build());
    }

    /**
     * 使用提供的webClient实例创建一个WebClientSession。
     *
     * @param webClient webClient实例
     * @return 创建的客户端
     */
    static WebClientSession create(WebClient webClient, CookieStore cookieStore) {
        return new WebClientSessionAware(webClient, cookieStore);
    }

    /**
     * 配置客户端，用于向每个请求添加HTTP标头
     *
     * @param name  header的名称
     * @param value header的值
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientSession addHeader(CharSequence name, CharSequence value);

    /**
     * 配置客户端，用于向每个请求添加HTTP标头
     *
     * @param name  header的名称
     * @param value header的值
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientSession addHeader(String name, String value);

    /**
     * 配置客户端，用于向每个请求添加HTTP标头
     *
     * @param name   header的名称
     * @param values header的值
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientSession addHeader(CharSequence name, Iterable<CharSequence> values);

    /**
     * 配置客户端，用于向每个请求添加HTTP标头
     *
     * @param name   header的名称
     * @param values header的值
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientSession addHeader(String name, Iterable<String> values);

    /**
     * 删除之前的添加的header
     *
     * @param name header的名称
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientSession removeHeader(CharSequence name);

    /**
     * 删除之前的添加的header
     *
     * @param name header的名称
     * @return 返回当前设置的对象，用于链式调用
     */
    WebClientSession removeHeader(String name);

    /**
     * 返回这个客户端的{@code CookieStore}
     * <p>
     * 所有添加到的cookie的参数会随着每一个请求一起发送
     * CookieStore认可接收到的cookie的domain、path、secure和max-age属性，
     * 并使用此客户端接收到的响应的cookie自动更新。
     *
     * @return 这个客户端的{@code CookieStore}
     */
    CookieStore cookieStore();
}