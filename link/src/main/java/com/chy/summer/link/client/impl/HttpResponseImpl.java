package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.common.codec.impl.BodyCodecImpl;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;

import java.util.List;

/**
 * HTTP响应封装实现
 *
 * @param <T>
 */
public class HttpResponseImpl<T> implements HttpResponse<T> {
    /**
     * HTTP版本
     */
    private final HttpVersion version;
    /**
     * 响应状态码
     */
    private final int statusCode;
    /**
     * 状态码信息
     */
    private final String statusMessage;
    /**
     * 请求头
     */
    private final MultiMap headers;
    /**
     * trailer信息
     */
    private final MultiMap trailers;
    /**
     * cookies headers
     */
    private final List<String> cookies;
    /**
     * body信息
     */
    private final T body;
    /**
     * 重定向的列表
     */
    private final List<String> redirects;

    public HttpResponseImpl(HttpVersion version,
                            int statusCode,
                            String statusMessage,
                            MultiMap headers,
                            MultiMap trailers,
                            List<String> cookies,
                            T body, List<String> redirects) {
        this.version = version;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = headers;
        this.trailers = trailers;
        this.cookies = cookies;
        this.body = body;
        this.redirects = redirects;
    }

    @Override
    public HttpVersion version() {
        return version;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String statusMessage() {
        return statusMessage;
    }

    @Override
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    @Override
    public MultiMap trailers() {
        return trailers;
    }

    @Override
    public String getTrailer(String trailerName) {
        return trailers.get(trailerName);
    }

    @Override
    public List<String> cookies() {
        return cookies;
    }

    @Override
    public MultiMap headers() {
        return headers;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public Buffer bodyAsBuffer() {
        return body instanceof Buffer ? (Buffer) body : null;
    }

    @Override
    public List<String> followedRedirects() {
        return redirects;
    }

    @Override
    public JsonArray bodyAsJsonArray() {
        Buffer b = bodyAsBuffer();
        return b != null ? BodyCodecImpl.JSON_ARRAY_DECODER.apply(b) : null;
    }


}