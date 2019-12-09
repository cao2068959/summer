package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.HttpRequest;
import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.common.codec.spi.BodyStream;
import com.chy.summer.link.common.multipart.MultipartForm;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.ReadStream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * http上下文
 */
public class HttpContext<T> {

  private final Context context;
  private final Handler<AsyncResult<HttpResponse<T>>> handler;
  /**
   * HTTP客户端实例
   */
  private final HttpClientImpl client;
  /**
   * 拦截器
   */
  private final List<Handler<HttpContext<?>>> interceptors;
  /**
   * 当前的HTTP请求
   */
  private HttpRequestImpl<T> request;
  /**
   * 发送的body
   */
  private Object body;
  /**
   * 请求的内容类型
   */
  private String contentType;
  private Map<String, Object> attrs;
  /**
   * 拦截器的迭代器
   */
  private Iterator<Handler<HttpContext<?>>> it;
  /**
   * 当前事件阶段,HTTP所属的阶段
   */
  private ClientPhase phase;
  /**
   * 基本的HTTP请求
   */
  private HttpClientRequest clientRequest;
  /**
   * 基本的HTTP响应
   */
  private HttpClientResponse clientResponse;
  private HttpResponse<T> response;
  /**
   * 发送错误时抛出的异常
   */
  private Throwable failure;
  /**
   * 跟随重定向的次数，初始值为0
   */
  private int redirects;
  /**
   * 所有跟踪的重定向
   */
  private List<String> redirectedLocations = new ArrayList<>();

  HttpContext(Context context, HttpClientImpl client, List<Handler<HttpContext<?>>> interceptors, Handler<AsyncResult<HttpResponse<T>>> handler) {
    this.context = context;
    this.handler = handler;
    this.client = client;
    this.interceptors = interceptors;
  }

  /**
   * @return 返回基本的HTTP请求，此方法仅在{@link ClientPhase#SEND_REQUEST}和之后的状态中使用
   */
  public HttpClientRequest clientRequest() {
    return clientRequest;
  }

  /**
   * @return 返回基本的HTTP响应，此方法仅在{@link ClientPhase#RECEIVE_RESPONSE}和之后的状态中使用
   */
  public HttpClientResponse clientResponse() {
    return clientResponse;
  }

  /**
   * @return 返回当前事件阶段
   */
  public ClientPhase phase() {
    return phase;
  }

  /**
   * @return 返回当前的请求对象
   */
  public HttpRequest<T> request() {
    return request;
  }

  /**
   * @return 返回当前响应对象，此方法仅在{@link ClientPhase#DISPATCH_RESPONSE}和之后的状态中使用
   */
  public HttpResponse<T> response() {
    return response;
  }

  /**
   * 设置当前的响应对象
   * @return 返回当前设置的对象，用于链式调用
   */
  public HttpContext<T> response(HttpResponse<T> response) {
    this.response = response;
    return this;
  }

  /**
   * @return 返回跟随重定向的次数，初始值为0
   */
  public int redirects() {
    return redirects;
  }

  /**
   * 设置跟随重定向的次数
   *
   * @param redirects 一个新的次数
   * @return 返回当前设置的对象，用于链式调用
   */
  public HttpContext<T> redirects(int redirects) {
    this.redirects = redirects;
    return this;
  }

  /**
   * @return 返回请求内容类型
   */
  public String contentType() {
    return contentType;
  }

  /**
   * @return 返回发送的body
   */
  public Object body() {
    return body;
  }

  /**
   * @return 失败, 只有在{@link ClientPhase#FAILURE}阶段的时候可以调用
   */
  public Throwable failure() {
    return failure;
  }

  /**
   * @return 返回所有跟踪的重定向
   */
  public List<String> getRedirectedLocations() {
    return redirectedLocations;
  }

  /**
   * 准备HTTP请求，属于{@link ClientPhase#PREPARE_REQUEST}阶段，会进行以下操作
   * 遍历拦截器链
   * 将阶段设置到{@link ClientPhase#PREPARE_REQUEST}
   */
  public void prepareRequest(HttpRequest<T> request, String contentType, Object body) {
    //设置HTTP请求
    this.request = (HttpRequestImpl<T>) request;
    //设置内容的类型
    this.contentType = contentType;
    //设置body
    this.body = body;
    //设置阶段
    fire(ClientPhase.PREPARE_REQUEST);
  }

  /**
   * 发送HTTP请求，属于{@link ClientPhase#SEND_REQUEST}阶段，会进行以下操作
   * 创建{@link HttpClientRequest}
   * 遍历拦截器链
   * 发送实际要求
   */
  public void sendRequest(HttpClientRequest clientRequest) {
    this.clientRequest = clientRequest;
    fire(ClientPhase.SEND_REQUEST);
  }

  /**
   * 进行重定向, 属于{@link ClientPhase#FOLLOW_REDIRECT}阶段，会进行以下操作
   * 遍历拦截器链
   * 发送实际要求
   */
  public void followRedirect() {
    fire(ClientPhase.SEND_REQUEST);
  }

  /**
   * 接收HTTP响应，属于{@link ClientPhase#RECEIVE_RESPONSE}阶段，会进行以下操作
   * 遍历拦截器链
   * 将阶段设置到{@link ClientPhase#DISPATCH_RESPONSE}
   */
  public void receiveResponse(HttpClientResponse clientResponse) {
    //获取状态码
    int sc = clientResponse.statusCode();
    //获取重定向的最多次数
    int maxRedirects = request.followRedirects ? client.getOptions().getMaxRedirects(): 0;
    if (redirects < maxRedirects && sc >= 300 && sc < 400) {
      //处理重定向，增加重定向次数
      redirects++;
      //进行重定向的处理
      Future<HttpClientRequest> next = client.redirectHandler().apply(clientResponse);
      if (next != null) {
        //添加转跳地址
        redirectedLocations.add(clientResponse.getHeader(HttpHeaders.LOCATION));
        //设置下一次的处理器
        next.setHandler(ar -> {
          if (ar.succeeded()) {
            //获取处理结果
            HttpClientRequest nextRequest = ar.result();
            if (request.headers != null) {
              //添加请求头
              nextRequest.headers().addAll(request.headers);
            }
            //保存成当前的请求
            this.clientRequest = nextRequest;
            //保存成当前的响应
            this.clientResponse = clientResponse;
            fire(ClientPhase.FOLLOW_REDIRECT);
          } else {
            fail(ar.cause());
          }
        });
        return;
      }
    }
    this.clientResponse = clientResponse;
    fire(ClientPhase.RECEIVE_RESPONSE);
  }

  /**
   * 处理HTTP响应，属于{@link ClientPhase#DISPATCH_RESPONSE}阶段，会进行以下操作
   * 创建{@link HttpResponse}
   * 遍历拦截器链
   * 将响应传递给response处理程序
   */
  public void dispatchResponse(HttpResponse<T> response) {
    this.response = response;
    fire(ClientPhase.DISPATCH_RESPONSE);
  }

  /**
   * 当前HTTP上下文发送了失败, 属于{@link ClientPhase#FAILURE}阶段，会进行以下操作
   * 遍历拦截器链
   * 将响应传递给response处理程序
   *
   * @param cause the failure cause
   * @return {@code true} if the failure can be dispatched
   */
  public boolean fail(Throwable cause) {
    if (phase == ClientPhase.FAILURE) {
      // 已经处理失败了
      return false;
    }
    //保存抛出的异常
    failure = cause;
    //找到相应的拦截器
    fire(ClientPhase.FAILURE);
    return true;
  }

  /**
   * 执行链中的下一个拦截器
   */
  public void next() {
    if (it.hasNext()) {
      //获取下一个拦截器
      Handler<HttpContext<?>> next = it.next();
      //执行处理器
      next.handle(this);
    } else {
      it = null;
      //执行对应阶段的处理
      execute();
    }
  }

  private void fire(ClientPhase phase) {
    //设置HTTP阶段
    this.phase = phase;
    //获取拦截器的迭代
    this.it = interceptors.iterator();
    //执行拦截器
    next();
  }

  /**
   * 匹配每个阶段对应的处理器
   */
  private void execute() {
    switch (phase) {
      case PREPARE_REQUEST:
        handlePrepareRequest();
        break;
      case SEND_REQUEST:
        handleSendRequest();
        break;
      case FOLLOW_REDIRECT:
        followRedirect();
        break;
      case RECEIVE_RESPONSE:
        handleReceiveResponse();
        break;
      case DISPATCH_RESPONSE:
        handleDispatchResponse();
        break;
      case FAILURE:
        handleFailure();
        break;
    }
  }

  /**
   * 失败阶段的处理器
   */
  private void handleFailure() {
    handler.handle(Future.failedFuture(failure));
  }

  /**
   * response已创建完成，即将被分派进行相应的处理
   */
  private void handleDispatchResponse() {
    handler.handle(Future.succeededFuture(response));
  }

  /**
   * 尚未创建HttpClientRequest实例，可以完全修改request中的任何配置
   */
  private void handlePrepareRequest() {
    HttpClientRequest req;
    String requestURI;
    if (request.params != null && request.params.size() > 0) {
      //查询条件拼接工具初始化
      QueryStringEncoder enc = new QueryStringEncoder(request.uri);
      //添加参数
      request.params.forEach(param -> enc.addParam(param.getKey(), param.getValue()));
      //生成URI
      requestURI = enc.toString();
    } else {
      //没有参数的情况下就不需要进行拼接了
      requestURI = request.uri;
    }
    //获取端口号
    int port = request.port;
    //获取主机地址
    String host = request.host;
    if (request.ssl != null && request.ssl != request.options.isSsl()) {
      //启动ssl
      req = client.request(request.method, request.serverAddress, new RequestOptions().setSsl(request.ssl).setHost(host).setPort
        (port)
        .setURI
          (requestURI));
    } else {
      if (request.protocol != null && !request.protocol.equals("http") && !request.protocol.equals("https")) {
        //不是http协议 也不是https协议
        try {
          //必须创建一个url才能进行HttpClient解析
          URI uri = new URI(request.protocol, null, host, port, requestURI, null, null);
          //创建一个请求客户端
          req = client.requestAbs(request.method, request.serverAddress, uri.toString());
        } catch (URISyntaxException ex) {
          //发送错误
          fail(ex);
          return;
        }
      } else {
        //http或者https协议，直接创建客户端就可以了
        req = client.request(request.method, request.serverAddress, port, host, requestURI);
      }
    }
    if (request.virtualHost != null) {
      //处理虚拟地址
      String virtalHost = request.virtualHost;
      if (port != 80) {
        //拼接端口号
        virtalHost += ":" + port;
      }
      //覆盖掉原始路径
      req.setHost(virtalHost);
    }
    //初始化重定向次数
    redirects = 0;
    if (request.headers != null) {
      req.headers().addAll(request.headers);
    }
    if (request.rawMethod != null) {
      req.setRawMethod(request.rawMethod);
    }
    //发送请求
    sendRequest(req);
  }

  /**
   * 已收到HttpClientResponse，并即将创建response的处理器
   */
  private void handleReceiveResponse() {
    //获取到响应
    HttpClientResponse resp = clientResponse;
    //获取上下文
    Context context = Vertx.currentContext();
    //异步协调
    Promise<HttpResponse<T>> promise = Promise.promise();
    //设置异步处理器
    promise.future().setHandler(r -> {
      //在上下文上运行（HTTP客户端需要），参数r是传入进来的处理器
      context.runOnContext(v -> {
        if (r.succeeded()) {
          //处理成功，传入结果
          dispatchResponse(r.result());
        } else {
          //处理失败，传入抛出的异常
          fail(r.cause());
        }
      });
    });
    //执行异常处理器
    resp.exceptionHandler(err -> {
      //判断是否已经执行完成
      if (!promise.future().isComplete()) {
        promise.fail(err);
      }
    });
    //创建一个管道
    Pipe<Buffer> pipe = resp.pipe();
    request.codec.create(ar1 -> {
      //处理器
      if (ar1.succeeded()) {
        BodyStream<T> stream = ar1.result();
        //管道处理流,传输流
        pipe.to(stream, ar2 -> {
          if (ar2.succeeded()) {
            //设置流处理完之后的回调方法
            stream.result().setHandler(ar3 -> {
              if (ar3.succeeded()) {
                //如果处理成功，设置完成之后的结果，创建HttpResponseImpl
                promise.complete(new HttpResponseImpl<T>(
                  //调用构造方法
                  resp.version(),
                  resp.statusCode(),
                  resp.statusMessage(),
                  resp.headers(),
                  resp.trailers(),
                  resp.cookies(),
                  stream.result().result(),
                  redirectedLocations
                ));
              } else {
                //处理失败
                promise.fail(ar3.cause());
              }
            });
          } else {
            promise.fail(ar2.cause());
          }
        });
      } else {
        //关闭流
        pipe.close();
        fail(ar1.cause());
      }
    });
  }

  /**
   * HttpClientRequest已创建但尚未发送，HTTP方法、URI或请求参数已经无法修改
   */
  private void handleSendRequest() {
    Promise<HttpClientResponse> responseFuture = Promise.<HttpClientResponse>promise();
    responseFuture.future().setHandler(ar -> {
      if (ar.succeeded()) {
        HttpClientResponse resp = ar.result();
        resp.pause();
        //接收响应，处理重定向
        receiveResponse(resp);
      } else {
        fail(ar.cause());
      }
    });
    //基本的HTTP请求
    HttpClientRequest req = clientRequest;
    req.setHandler(ar -> {
      if (ar.succeeded()) {
        //尝试成功
        responseFuture.tryComplete(ar.result());
      } else {
        //尝试失败
        responseFuture.tryFail(ar.cause());
      }
    });
    if (request.timeout > 0) {
      //设置超时时间
      req.setTimeout(request.timeout);
    }
    if (contentType != null) {
      //获取content_type
      String prev = req.headers().get(HttpHeaders.CONTENT_TYPE);
      if (prev == null) {
        //如果空的直接设置就好了
        req.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
      } else {
        //如果不是空的就需要更新上下文里的值
        contentType = prev;
      }
    }
    if (body != null || "application/json".equals(contentType)) {
      if (body instanceof MultiMap) {
        //拼接Form，切片后面的参数
        MultipartForm parts = MultipartForm.create();
        MultiMap attributes = (MultiMap) body;
        for (Map.Entry<String, String> attribute : attributes) {
          parts.attribute(attribute.getKey(), attribute.getValue());
        }
        body = parts;
      }
      if (body instanceof MultipartForm) {
        //multipart/form-data方式的请求
        MultipartFormUpload multipartForm;
        try {
          boolean multipart = "multipart/form-data".equals(contentType);
          //对切片的处理
          HttpPostRequestEncoder.EncoderMode encoderMode = request.multipartMixed ? HttpPostRequestEncoder.EncoderMode.RFC1738 : HttpPostRequestEncoder.EncoderMode.HTML5;
          multipartForm = new MultipartFormUpload(context,  (MultipartForm) this.body, multipart, encoderMode);
          //body赋值
          this.body = multipartForm;
        } catch (Exception e) {
          responseFuture.tryFail(e);
          return;
        }
        for (String headerName : request.headers().names()) {
          //处理header
          req.putHeader(headerName, request.headers().get(headerName));
        }
        multipartForm.headers().forEach(header -> {
          //将所有的header放入request
          req.putHeader(header.getKey(), header.getValue());
        });
        multipartForm.run();
      }

      if (body instanceof ReadStream<?>) {
        //流处理的方式
        ReadStream<Buffer> stream = (ReadStream<Buffer>) body;
        if (request.headers == null || !request.headers.contains(HttpHeaders.CONTENT_LENGTH)) {
          //启动分块传输
          req.setChunked(true);
        }
        stream.pipeTo(req, ar -> {
          //管道初始失败的处理
          if (ar.failed()) {
            responseFuture.tryFail(ar.cause());
            req.reset();
          }
        });
      } else {
        //缓存字符的处理方式
        Buffer buffer;
        if (body instanceof Buffer) {
          //字符串的处理
          buffer = (Buffer) body;
        } else if (body instanceof JsonObject) {
          //json的处理
          buffer = Buffer.buffer(((JsonObject)body).encode());
        } else {
          buffer = Buffer.buffer(Json.encode(body));
        }
        //设置异常处理器
        req.exceptionHandler(responseFuture::tryFail);
        //结束request请求的设置，并且发送buffer
        req.end(buffer);
      }
    } else {
      //除了"application/json"之外的处理  主要是text
      req.exceptionHandler(responseFuture::tryFail);
      req.end();
    }
  }

  public <T> T get(String key) {
    return attrs != null ? (T) attrs.get(key) : null;
  }

  public HttpContext<T> set(String key, Object value) {
    if (value == null) {
      if (attrs != null) {
        attrs.remove(key);
      }
    } else {
      if (attrs == null) {
        attrs = new HashMap<>();
      }
      attrs.put(key, value);
    }
    return this;
  }
}