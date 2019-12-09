package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.WebClient;
import com.chy.summer.link.client.WebClientSession;
import com.chy.summer.link.client.spi.CookieStore;
import io.vertx.core.http.CaseInsensitiveHeaders;

/**
 * 异步会话 HTTP和HTTP/2客户端实现
 */
public class WebClientSessionAware extends WebClientBase implements WebClientSession {

    /**
     * cookie
     */
    private final CookieStore cookieStore;
    /**
     * 请求头，不区分大小写
     */
    private CaseInsensitiveHeaders headers;

    public WebClientSessionAware(WebClient webClient, CookieStore cookieStore) {
        super((WebClientBase) webClient);
        this.cookieStore = cookieStore;
        //添加session的拦截器
        addInterceptor(new SessionAwareInterceptor());
    }

    @Override
    public CookieStore cookieStore() {
        return cookieStore;
    }

    /**
     * 获取请求头
     */
    protected CaseInsensitiveHeaders headers() {
        if (headers == null) {
            headers = new CaseInsensitiveHeaders();
        }
        return headers;
    }

    @Override
    public WebClientSession addHeader(CharSequence name, CharSequence value) {
        headers().add(name, value);
        return this;
    }

    @Override
    public WebClientSession addHeader(String name, String value) {
        headers().add(name, value);
        return this;
    }

    @Override
    public WebClientSession addHeader(CharSequence name, Iterable<CharSequence> values) {
        headers().add(name, values);
        return this;
    }

    @Override
    public WebClientSession addHeader(String name, Iterable<String> values) {
        headers().add(name, values);
        return this;
    }

    @Override
    public WebClientSession removeHeader(CharSequence name) {
        headers().remove(name);
        return this;
    }

    @Override
    public WebClientSession removeHeader(String name) {
        headers().remove(name);
        return this;
    }

}