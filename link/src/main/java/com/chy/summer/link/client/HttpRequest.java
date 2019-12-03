//package com.chy.summer.link.client;
//
//import io.vertx.core.Handler;
//import io.vertx.core.MultiMap;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.streams.ReadStream;
//
///**
// * 客户端HTTP请求接口
// * 实例是由WebClient实例通过与特定HTTP方法相对应的一个方法创建
// * 该请求应在发送之前进行配置，并且请求不可变
// * 再配置请求后，可以调用一下方法，执行实际的请求，可以多次进行调用来在不同的时间点执行相同的HTTP请求
// * {@link #send(Handler)}
// * {@link #sendStream(ReadStream, Handler)}
// * {@link #sendJson(Object, Handler)} ()}
// * {@link #sendForm(MultiMap, Handler)}
// * 收到HTTP响应后，将使用HttpResponse实例处理该程序的回调
// * 如果HTTP请求失败（如：连接错误）或一直无法获取到HTTP的响应（如：连接或解组错误）时失败
// * @param <T> 请求返回体的类型
// */
//public interface HttpRequest<T> {
//
//  /**
//   * 配置请求的请求类型
//   * OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT, PATCH, OTHER;
//   *
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> method(HttpMethod value);
//
//  /**
//   * 配置请求的请求类型
//   *
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> rawMethod(String method);
//
//  /**
//   * 配置请求使用的端口号
//   *
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> port(int value);
//
//  /**
//   * 配置请求的编码响应器,用于响应返回的对应编码
//   *
//   * @param responseCodec 编码响应器
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  <U> HttpRequest<U> as(BodyCodec<U> responseCodec);
//
//  /**
//   * 配置请求使用的host
//   *
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> host(String value);
//
//  /**
//   * 配置请求使用的虚拟host
//   *
//   * 通常情况下，host是设置在请求头的host参数里，所以可以通过host参数可以解析到服务器的ip地址
//   * 但有的时候，我们需要为一个无法解析成ip地址的路径设置一个host，这个虚拟的host路径将覆盖实际的host
//   * 虚拟host也可以用于SNI
//   *
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> virtualHost(String value);
//
//  /**
//   * 配置请求的URI
//   * 如果uri上有查询参数，可以通过{@link #queryParams()}方法获取到参数的多重映射对象，当前的会覆盖先前设置的所有参数
//   *
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> uri(String value);
//
//  /**
//   * 配置请求头
//   *
//   * @param headers http的请求头
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> putHeaders(MultiMap headers);
//
//  /**
//   * 在请求头中添加一个新的参数
//   *
//   * @param name 请求头参数名称
//   * @param value 请求头参数值
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> putHeader(String name, String value);
//
//  /**
//   * 在请求头中添加一个有多个值的新参数
//   *
//   * @param name 请求头参数名称
//   * @param value 请求头参数值
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> putHeader(String name, Iterable<String> value);
//
//  /**
//   * 获取请求头
//   * @return 请求头
//   */
//  MultiMap headers();
//
//  /**
//   * 配置请求的HTTP basic身份认证
//   *
//   * 在一个标准的Http basic身份认证中，需要在请求头中包含一个格式为“Authorization:授权凭证”的参数，
//   * 其中凭证是由  id:密码  的base64编码的形式组成
//   *
//   * @param id 身份id
//   * @param password 对应的密码
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> basicAuthentication(String id, String password);
//
//  /**
//   * 配置请求的HTTP basic身份认证（buffer类型的参数）
//   *
//   * 在一个标准的Http basic身份认证中，需要在请求头中包含一个格式为“Authorization:授权凭证”的参数，
//   * 其中凭证是由  id:密码  的base64编码的形式组成
//   *
//   * @param id 身份id
//   * @param password 对应的密码
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> basicAuthentication(Buffer id, Buffer password);
//
//  /**
//   * 配置请求的bearer令牌身份认证
//   *
//   * 在使用OAuth2.0的协议中，需要在请求头中包含一个格式为“Authorization:Bearer 令牌”，
//   * 其中令牌是授权服务器发给客户端的校验凭证，用于访问受保护的资源
//   *
//   * @param bearerToken Bearer令牌
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> bearerTokenAuthentication(String bearerToken);
//
//  /**
//   * 是否启用ssl
//   * @param value {@code true}启用  {@code false}关闭
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> ssl(Boolean value);
//
//  /**
//   * 配置请求超时时间（单位毫秒）
//   *
//   * 如果请求在超时时间内未能返回任何数据，则会抛出{@link java.util.concurrent.TimeoutException}异常使请求失败
//   * 设置0或者负数，没有超时时间
//   *
//   * @param value 以毫秒为单位的时间量
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> timeout(long value);
//
//  /**
//   * 添加一个查询参数到请求中
//   *
//   * @param paramName 参数名
//   * @param paramValue 参数值
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> addQueryParam(String paramName, String paramValue);
//
//  /**
//   * 为请求设置查询参数
//   *
//   * @param paramName 参数名
//   * @param paramValue 参数值
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> setQueryParam(String paramName, String paramValue);
//
//  /**
//   * 是否根据请求的相应自动进行重定向
//   *
//   * @param value {@code true}自动重定向  {@code false}不重定向
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> followRedirects(boolean value);
//
//  /**
//   * 为请求添加一个断言来判断响应是否有效（可以添加多个断言）
//   *
//   * @param predicate 断言
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  default HttpRequest<T> expect(Function<HttpResponse<Void>, ResponsePredicateResult> predicate) {
//    return expect(predicate::apply);
//  }
//
//  /**
//   * 为请求添加一个断言来判断响应是否有效（可以添加多个断言）
//   *
//   * @param predicate 断言
//   * @return 返回当前设置的HttpRequest，用于链式调用
//   */
//  HttpRequest<T> expect(ResponsePredicate predicate);
//
//  /**
//   * 获取当前请求的查询参数
//   *
//   * @return 当前查询参数
//   */
//  MultiMap queryParams();
//
//  /**
//   * Copy this request
//   *
//   * @return a copy of this request
//   */
//  HttpRequest<T> copy();
//
//  /**
//   * Allow or disallow multipart mixed encoding when sending {@link MultipartForm} having files sharing the same
//   * file name.
//   * <br/>
//   * The default value is {@code true}.
//   * <br/>
//   * Set to {@code false} if you want to achieve the behavior for <a href="http://www.w3.org/TR/html5/forms.html#multipart-form-data">HTML5</a>.
//   *
//   * @param allow {@code true} allows use of multipart mixed encoding
//   * @return a reference to this, so the API can be used fluently
//   */
//  HttpRequest<T> multipartMixed(boolean allow);
//
//  /**
//   * Like {@link #send(Handler)} but with an HTTP request {@code body} stream.
//   *
//   * @param body the body
//   */
//  void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#sendStream(ReadStream, Handler)
//   * @param body the body
//   */
//  default Future<HttpResponse<T>> sendStream(ReadStream<Buffer> body) {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    sendStream(body, promise);
//    return promise.future();
//  }
//
//  /**
//   * Like {@link #send(Handler)} but with an HTTP request {@code body} buffer.
//   *
//   * @param body the body
//   */
//  void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#sendBuffer(Buffer, Handler)
//   * @param body the body
//   */
//  default Future<HttpResponse<T>> sendBuffer(Buffer body) {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    sendBuffer(body, promise);
//    return promise.future();
//  }
//
//  /**
//   * Like {@link #send(Handler)} but with an HTTP request {@code body} object encoded as json and the content type
//   * set to {@code application/json}.
//   *
//   * @param body the body
//   */
//  void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#sendJsonObject(JsonObject, Handler)
//   * @param body the body
//   */
//  default Future<HttpResponse<T>> sendJsonObject(JsonObject body) {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    sendJsonObject(body, promise);
//    return promise.future();
//  }
//
//  /**
//   * Like {@link #send(Handler)} but with an HTTP request {@code body} object encoded as json and the content type
//   * set to {@code application/json}.
//   *
//   * @param body the body
//   */
//  void sendJson(@Nullable Object body, Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#sendJson(Object, Handler)
//   * @param body the body
//   */
//  default Future<HttpResponse<T>> sendJson(@Nullable Object body) {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    sendJson(body, promise);
//    return promise.future();
//  }
//
//  /**
//   * Like {@link #send(Handler)} but with an HTTP request {@code body} multimap encoded as form and the content type
//   * set to {@code application/x-www-form-urlencoded}.
//   * <p>
//   * When the content type header is previously set to {@code multipart/form-data} it will be used instead.
//   *
//   * @param body the body
//   */
//  void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#sendForm(MultiMap, Handler)
//   * @param body the body
//   */
//  default Future<HttpResponse<T>> sendForm(MultiMap body) {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    sendForm(body, promise);
//    return promise.future();
//  }
//
//  /**
//   * Like {@link #send(Handler)} but with an HTTP request {@code body} multimap encoded as form and the content type
//   * set to {@code multipart/form-data}. You may use this method to send attributes and upload files.
//   *
//   * @param body the body
//   */
//  void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#sendMultipartForm(MultipartForm, Handler)
//   * @param body the body
//   */
//  default Future<HttpResponse<T>> sendMultipartForm(MultipartForm body) {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    sendMultipartForm(body, promise);
//    return promise.future();
//  }
//
//  /**
//   * Send a request, the {@code handler} will receive the response as an {@link HttpResponse}.
//   */
//  void send(Handler<AsyncResult<HttpResponse<T>>> handler);
//
//  /**
//   * @see HttpRequest#send(Handler)
//   */
//  default Future<HttpResponse<T>> send() {
//    Promise<HttpResponse<T>> promise = Promise.promise();
//    send(promise);
//    return promise.future();
//  }
//}