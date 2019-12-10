package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.spi.CookieStore;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClientRequest;

import java.net.URI;
import java.util.List;

/**
 * session的拦截器
 */
public class SessionAwareInterceptor implements Handler<HttpContext<?>> {

  /**
   * header上下文的key
   */
  private static final String HEADERS_CONTEXT_KEY = "_originalHeaders";

  /**
   * 处理器
   */
  @Override
  public void handle(HttpContext<?> context) {
    switch(context.phase()) {
    case PREPARE_REQUEST:
      //尚未创建HttpClientRequest实例的阶段
      prepareRequest(context);
      break;
    case FOLLOW_REDIRECT:
      //接收到HttpClientResponse，可能需要重定向
      processRedirectCookies(context);
      break;
    case DISPATCH_RESPONSE:
      //response已创建完成，即将进行处理
      processResponse(context);
	    break;
    default:
      break;
    }
    
    context.next();
  }

  /**
   * 尚未创建HttpClientRequest实例的阶段
   */
  private void prepareRequest(HttpContext<?> context) {
    //获取请求实例
    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    //获取web客户端
    WebClientSessionAware webclient = (WebClientSessionAware) request.client;
    //找到header
    MultiMap headers = context.get(HEADERS_CONTEXT_KEY);
    if (headers == null) {
      //没有header，就用请求的header生成一个CaseInsensitiveHeaders（不分大小写的header）
      headers = new CaseInsensitiveHeaders().addAll(request.headers());
      //存入上下文
      context.set(SessionAwareInterceptor.HEADERS_CONTEXT_KEY, headers);
    }
    
    //每次发送之前重置一下header，如何添加请求头和客户端的请求头
    request.headers().clear().addAll(headers).addAll(webclient.headers());

    //网络地址
    String domain = request.virtualHost;
    if (domain == null) {
      domain = request.host;
    }
    //cookie的配置
    Iterable<Cookie> cookies = webclient.cookieStore().get(request.ssl, domain, request.uri);
    for (Cookie c : cookies) {
      request.headers().add("cookie", ClientCookieEncoder.STRICT.encode(c));
    }
  }

  /**
   * 接收到HttpClientResponse的阶段，可能需要重定向
   */
  private void processRedirectCookies(HttpContext<?> context) {
    //处理重定向的响应
    this.processRedirectResponse(context);
    //准备重定向请求
    this.prepareRedirectRequest(context);
  }

  /**
   * 处理重定向的响应
   */
  private void processRedirectResponse(HttpContext<?> context) {
    //这个时候 context在clientRequest()中包含了重定向请求，而在request()中包含原始请求
    List<String> cookieHeaders = context.clientResponse().cookies();
    if (cookieHeaders == null) {
      return;
    }

    //获取web客户端
    WebClientSessionAware webclient = (WebClientSessionAware) ((HttpRequestImpl)context.request()).client;
    HttpRequestImpl<?> originalRequest = (HttpRequestImpl<?>) context.request();
    //获取cookie的储存
    CookieStore cookieStore = webclient.cookieStore();
    //从请求中获取host
    String domain = URI.create(context.clientResponse().request().absoluteURI()).getHost();
    if (domain.equals(originalRequest.host) && originalRequest.virtualHost != null) {
      //虚拟主机地址
      domain = originalRequest.virtualHost;
    }
    //最终的主机地址
    final String finalDomain = domain;
    cookieHeaders.forEach(header -> {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
      if (cookie != null) {
        if (cookie.domain() == null) {
          // 如果缺少域名，就设置域，用来发送Cookie
          cookie.setDomain(finalDomain);
        }
        //cookie存入coolie存储中
        cookieStore.put(cookie);
      }
    });
  }

  /**
   * 准备重定向请求
   */
  private void prepareRedirectRequest(HttpContext<?> context) {
    //这个时候 context在clientRequest()中包含了重定向请求，而在request()中包含原始请求
    HttpClientRequest redirectRequest = context.clientRequest();
    HttpRequestImpl<?> originalRequest = (HttpRequestImpl<?>) context.request();
    WebClientSessionAware webclient = (WebClientSessionAware) originalRequest.client;
    //获取请求头
    MultiMap headers = context.get(HEADERS_CONTEXT_KEY);
    if (headers == null) {
      //封装请求头
      headers = new CaseInsensitiveHeaders().addAll(redirectRequest.headers());
      context.set(SessionAwareInterceptor.HEADERS_CONTEXT_KEY, headers);
    }

    //获取host
    String redirectHost = URI.create(redirectRequest.absoluteURI()).getHost();
    String domain;
    if (redirectHost.equals(originalRequest.host) && originalRequest.virtualHost != null) {
      //使用虚拟主机地址
      domain = originalRequest.virtualHost;
    } else {
      //使用原主机地址
      domain = redirectHost;
    }

    //处理cookie
    Iterable<Cookie> cookies = webclient.cookieStore().get(originalRequest.ssl, domain, redirectRequest.path());
    for (Cookie c : cookies) {
      redirectRequest.headers().add("cookie", ClientCookieEncoder.STRICT.encode(c));
    }
  }

  /**
   * response已创建完成阶段
   */
  private void processResponse(HttpContext<?> context) {
    //处理一下cookie
    List<String> cookieHeaders = context.clientResponse().cookies();
    if (cookieHeaders == null) {
      return;
    }

    WebClientSessionAware webclient = (WebClientSessionAware) ((HttpRequestImpl)context.request()).client;
    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();
    CookieStore cookieStore = webclient.cookieStore();
    cookieHeaders.forEach(header -> {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
      if (cookie != null) {
        if (cookie.domain() == null) {
          // 如果缺少域名，就设置域，用来发送Cookie
          cookie.setDomain(request.virtualHost != null ? request.virtualHost : request.host);
        }
        cookieStore.put(cookie);
      }
    });
  }
}