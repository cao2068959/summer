package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.HttpRequest;
import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.client.WebClientOptions;
import com.chy.summer.link.client.impl.predicate.PredicateInterceptor;
import com.chy.summer.link.common.codec.impl.BodyCodecImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * web客户端的基类实现
 */
public class WebClientBase implements WebClientInternal {
    /**
     * HTTP客户端 持有
     */
    final HttpClient client;
    /**
     * web客户端配置 持有
     */
    final WebClientOptions options;

    /**
     * 请求/响应拦截器
     */
    private final List<Handler<HttpContext<?>>> interceptors;

    public WebClientBase(HttpClient client, WebClientOptions options) {
        this.client = client;
        this.options = new WebClientOptions(options);
        this.interceptors = new CopyOnWriteArrayList<>();

        // 添加基础拦截器（断言处理的拦截器）
        addInterceptor(new PredicateInterceptor());
    }

    /**
     * 复制webClient的构造函数
     */
    WebClientBase(WebClientBase webClient) {
        this.client = webClient.client;
        this.options = new WebClientOptions(webClient.options);
        this.interceptors = new CopyOnWriteArrayList<>(webClient.interceptors);
    }

    @Override
    public HttpRequest<Buffer> get(int port, String host, String requestURI) {
        return request(HttpMethod.GET, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> get(String requestURI) {
        return request(HttpMethod.GET, requestURI);
    }

    @Override
    public HttpRequest<Buffer> get(String host, String requestURI) {
        return request(HttpMethod.GET, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> getAbs(String absoluteURI) {
        return requestAbs(HttpMethod.GET, absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> post(String requestURI) {
        return request(HttpMethod.POST, requestURI);
    }

    @Override
    public HttpRequest<Buffer> post(String host, String requestURI) {
        return request(HttpMethod.POST, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> post(int port, String host, String requestURI) {
        return request(HttpMethod.POST, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(String requestURI) {
        return request(HttpMethod.PUT, requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(String host, String requestURI) {
        return request(HttpMethod.PUT, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(int port, String host, String requestURI) {
        return request(HttpMethod.PUT, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(String host, String requestURI) {
        return request(HttpMethod.DELETE, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(String requestURI) {
        return request(HttpMethod.DELETE, requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
        return request(HttpMethod.DELETE, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(String requestURI) {
        return request(HttpMethod.PATCH, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(String host, String requestURI) {
        return request(HttpMethod.PATCH, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
        return request(HttpMethod.PATCH, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(String requestURI) {
        return request(HttpMethod.HEAD, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(String host, String requestURI) {
        return request(HttpMethod.HEAD, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(int port, String host, String requestURI) {
        return request(HttpMethod.HEAD, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> raw(String customHttpMethod, String requestURI) {
        return request(HttpMethod.OTHER, requestURI).rawMethod(customHttpMethod);
    }

    @Override
    public HttpRequest<Buffer> raw(String customHttpMethod, int port, String host, String requestURI) {
        return request(HttpMethod.OTHER, port, host, requestURI).rawMethod(customHttpMethod);
    }

    @Override
    public HttpRequest<Buffer> raw(String customHttpMethod, String host, String requestURI) {
        return request(HttpMethod.OTHER, host, requestURI).rawMethod(customHttpMethod);
    }

    @Override
    public HttpRequest<Buffer> postAbs(String absoluteURI) {
        return requestAbs(HttpMethod.POST, absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> putAbs(String absoluteURI) {
        return requestAbs(HttpMethod.PUT, absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
        return requestAbs(HttpMethod.DELETE, absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> patchAbs(String absoluteURI) {
        return requestAbs(HttpMethod.PATCH, absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> headAbs(String absoluteURI) {
        return requestAbs(HttpMethod.HEAD, absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> rawAbs(String customHttpMethod, String absoluteURI) {
        return requestAbs(HttpMethod.OTHER, absoluteURI).rawMethod(customHttpMethod);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
        return request(method, (SocketAddress) null, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI) {
        return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), options.getDefaultPort(), options.getDefaultHost(),
                requestURI, BodyCodecImpl.BUFFER, options);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, RequestOptions requestOptions) {
        return request(method, null, requestOptions);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions requestOptions) {
        return new HttpRequestImpl<>(this, method, serverAddress, requestOptions.isSsl(), requestOptions.getPort(),
                requestOptions.getHost(), requestOptions.getURI(), BodyCodecImpl.BUFFER, options);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
        return request(method, null, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI) {
        return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), options.getDefaultPort(), host, requestURI, BodyCodecImpl.BUFFER, options);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
        return request(method, null, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI) {
        return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), port, host, requestURI, BodyCodecImpl.BUFFER, options);
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, String surl) {
        return requestAbs(method, null, surl);
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String surl) {
        URL url;
        try {
            //解析绝对URI
            url = new URL(surl);
        } catch (MalformedURLException e) {
            throw new VertxException("无效的url: " + surl);
        }
        boolean ssl = false;
        //获取端口号
        int port = url.getPort();
        //获取协议类型
        String protocol = url.getProtocol();
        if ("ftp".equals(protocol)) {
            //ftp协议，默认端口号为21
            if (port == -1) {
                port = 21;
            }
        } else {
            char chend = protocol.charAt(protocol.length() - 1);
            if (chend == 'p') {
                //http协议，最后一位是p，默认端口号为80
                if (port == -1) {
                    port = 80;
                }
            } else if (chend == 's') {
                //https协议，最后一位是s，默认端口号为443
                ssl = true;
                if (port == -1) {
                    port = 443;
                }
            }
        }
        return new HttpRequestImpl<>(this, method, serverAddress, protocol, ssl, port, url.getHost(), url.getFile(),
                BodyCodecImpl.BUFFER, options);
    }

    @Override
    public WebClientInternal addInterceptor(Handler<HttpContext<?>> interceptor) {
        interceptors.add((Handler) interceptor);
        return this;
    }

    @Override
    public <T> HttpContext<T> createContext(Handler<AsyncResult<HttpResponse<T>>> handler) {
        HttpClientImpl client = (HttpClientImpl) this.client;
        return new HttpContext<>(client.getVertx().getContext(), client, interceptors, handler);
    }

    @Override
    public void close() {
        client.close();
    }
}
