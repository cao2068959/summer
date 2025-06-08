package com.chy.summer.link.client;

import com.chy.summer.link.client.predicate.ResponsePredicate;
import com.chy.summer.link.client.predicate.ResponsePredicateResult;
import com.chy.summer.link.common.codec.BodyCodec;
import com.chy.summer.link.common.multipart.MultipartForm;
import javax.annotation.Nullable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

import java.util.function.Function;

/**
 * 客户端HTTP请求接口
 * 实例是由WebClient实例通过与特定HTTP方法相对应的一个方法创建
 * 该请求应在发送之前进行配置，并且请求不可变
 * 再配置请求后，可以调用一下方法，执行实际的请求，可以多次进行调用来在不同的时间点执行相同的HTTP请求
 * {@link #send(Handler)}
 * {@link #sendStream(ReadStream, Handler)}
 * {@link #sendJson(Object, Handler)} ()}
 * {@link #sendForm(MultiMap, Handler)}
 * 收到HTTP响应后，将使用HttpResponse实例处理该程序的回调
 * 如果HTTP请求失败（如：连接错误）或一直无法获取到HTTP的响应（如：连接或解组错误）时失败
 *
 * @param <T> 请求返回体的类型
 */
public interface HttpRequest<T> {

    /**
     * 配置请求的请求类型
     * OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT, PATCH, OTHER;
     *
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> method(HttpMethod value);

    /**
     * 配置请求的请求类型
     *
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> rawMethod(String method);

    /**
     * 配置请求使用的端口号
     *
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> port(int value);

    /**
     * 配置请求的编码响应器,用于响应返回的对应编码
     *
     * @param responseCodec 编码响应器
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    <U> HttpRequest<U> as(BodyCodec<U> responseCodec);

    /**
     * 配置请求使用的host
     *
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> host(String value);

    /**
     * 配置请求使用的虚拟host
     * <p>
     * 通常情况下，host是设置在请求头的host参数里，所以可以通过host参数可以解析到服务器的ip地址
     * 但有的时候，我们需要为一个无法解析成ip地址的路径设置一个host，这个虚拟的host路径将覆盖实际的host
     * 虚拟host也可以用于SNI
     *
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> virtualHost(String value);

    /**
     * 配置请求的URI
     * 如果uri上有查询参数，可以通过{@link #queryParams()}方法获取到参数的多重映射对象，当前的会覆盖先前设置的所有参数
     *
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> uri(String value);

    /**
     * 配置请求头
     *
     * @param headers http的请求头
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> putHeaders(MultiMap headers);

    /**
     * 在请求头中添加一个新的参数
     *
     * @param name  请求头参数名称
     * @param value 请求头参数值
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> putHeader(String name, String value);

    /**
     * 在请求头中添加一个有多个值的新参数
     *
     * @param name  请求头参数名称
     * @param value 请求头参数值
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> putHeader(String name, Iterable<String> value);

    /**
     * 获取请求头
     *
     * @return 请求头
     */
    MultiMap headers();

    /**
     * 配置请求的HTTP basic身份认证
     * <p>
     * 在一个标准的Http basic身份认证中，需要在请求头中包含一个格式为“Authorization:授权凭证”的参数，
     * 其中凭证是由  id:密码  的base64编码的形式组成
     *
     * @param id       身份id
     * @param password 对应的密码
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> basicAuthentication(String id, String password);

    /**
     * 配置请求的HTTP basic身份认证（buffer类型的参数）
     * <p>
     * 在一个标准的Http basic身份认证中，需要在请求头中包含一个格式为“Authorization:授权凭证”的参数，
     * 其中凭证是由  id:密码  的base64编码的形式组成
     *
     * @param id       身份id
     * @param password 对应的密码
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> basicAuthentication(Buffer id, Buffer password);

    /**
     * 配置请求的bearer令牌身份认证
     * <p>
     * 在使用OAuth2.0的协议中，需要在请求头中包含一个格式为“Authorization:Bearer 令牌”，
     * 其中令牌是授权服务器发给客户端的校验凭证，用于访问受保护的资源
     *
     * @param bearerToken Bearer令牌
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> bearerTokenAuthentication(String bearerToken);

    /**
     * 是否启用ssl
     *
     * @param value {@code true}启用  {@code false}关闭
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> ssl(Boolean value);

    /**
     * 配置请求超时时间（单位毫秒）
     * <p>
     * 如果请求在超时时间内未能返回任何数据，则会抛出{@link java.util.concurrent.TimeoutException}异常使请求失败
     * 设置0或者负数，没有超时时间
     *
     * @param value 以毫秒为单位的时间量
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> timeout(long value);

    /**
     * 添加一个查询参数到请求中
     *
     * @param paramName  参数名
     * @param paramValue 参数值
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> addQueryParam(String paramName, String paramValue);

    /**
     * 为请求设置查询参数
     *
     * @param paramName  参数名
     * @param paramValue 参数值
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> setQueryParam(String paramName, String paramValue);

    /**
     * 是否根据请求的相应自动进行重定向
     *
     * @param value {@code true}自动重定向  {@code false}不重定向
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> followRedirects(boolean value);

    /**
     * 为请求添加一个断言来判断响应是否有效（可以添加多个断言）
     *
     * @param predicate 断言
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    default HttpRequest<T> expect(Function<HttpResponse<Void>, ResponsePredicateResult> predicate) {
        return expect(predicate::apply);
    }

    /**
     * 为请求添加一个断言来判断响应是否有效（可以添加多个断言）
     *
     * @param predicate 断言
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> expect(ResponsePredicate predicate);

    /**
     * 获取当前请求的查询参数
     *
     * @return 当前查询参数
     */
    MultiMap queryParams();

    /**
     * 拷贝这个请求
     *
     * @return 返回拷贝出来的请求
     */
    HttpRequest<T> copy();

    /**
     * 使用{@link MultipartForm}发送文件的时候，是否启用分片传输
     * 默认为true
     *
     * @param allow {@code true} 允许使用分片混合编码
     * @return 返回当前设置的HttpRequest，用于链式调用
     */
    HttpRequest<T> multipartMixed(boolean allow);

    /**
     * 发送一个带有请求主体的HTTP请求
     *
     * @param body 请求的主体
     */
    void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个带有请求主体的HTTP请求
     *
     * @param body 请求的主体
     */
    default Future<HttpResponse<T>> sendStream(ReadStream<Buffer> body) {
        Promise<HttpResponse<T>> promise = Promise.promise();
        sendStream(body, promise);
        return promise.future();
    }

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个Buffer
     *
     * @param body 请求的主体
     */
    void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个Buffer
     *
     * @param body 请求的主体
     */
    default Future<HttpResponse<T>> sendBuffer(Buffer body) {
        Promise<HttpResponse<T>> promise = Promise.promise();
        sendBuffer(body, promise);
        return promise.future();
    }

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个json，同时设置content的类型为application/json
     *
     * @param body 请求的主体
     */
    void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个json，同时设置content的类型为application/json
     *
     * @param body 请求的主体
     */
    default Future<HttpResponse<T>> sendJsonObject(JsonObject body) {
        Promise<HttpResponse<T>> promise = Promise.promise();
        sendJsonObject(body, promise);
        return promise.future();
    }

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个对象，传输时转成json，同时设置content的类型为application/json
     *
     * @param body 请求的主体
     */
    void sendJson(@Nullable Object body, Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个对象，传输时转成json，同时设置content的类型为application/json
     *
     * @param body 请求的主体
     */
    default Future<HttpResponse<T>> sendJson(@Nullable Object body) {
        Promise<HttpResponse<T>> promise = Promise.promise();
        sendJson(body, promise);
        return promise.future();
    }

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个MultiMap，同时设置content的类型为application/x-www-form-urlencoded
     * 当前请求头的content的类型预先设置为multipart/form-data的时候，将会使用它
     *
     * @param body 请求的主体
     */
    void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个MultiMap，同时设置content的类型为application/x-www-form-urlencoded
     * 当前请求头的content的类型预先设置为multipart/form-data的时候，将会使用它
     *
     * @param body 请求的主体
     */
    default Future<HttpResponse<T>> sendForm(MultiMap body) {
        Promise<HttpResponse<T>> promise = Promise.promise();
        sendForm(body, promise);
        return promise.future();
    }

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个MultiMap，同时设置content的类型为multipart/form-data
     * 可以使用此方法发送属性和上传文件
     *
     * @param body 请求的主体
     */
    void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个带有请求主体的HTTP请求，参数是一个MultiMap，同时设置content的类型为multipart/form-data
     * 可以使用此方法发送属性和上传文件
     *
     * @param body 请求的主体
     */
    default Future<HttpResponse<T>> sendMultipartForm(MultipartForm body) {
        Promise<HttpResponse<T>> promise = Promise.promise();
        sendMultipartForm(body, promise);
        return promise.future();
    }

    /**
     * 发送一个请求，将会以{@link HttpResponse}的形式接收响应
     */
    void send(Handler<AsyncResult<HttpResponse<T>>> handler);

    /**
     * 发送一个请求，将会以{@link HttpResponse}的形式接收响应
     */
    default Future<HttpResponse<T>> send() {
        Promise<HttpResponse<T>> promise = Promise.promise();
        send(promise);
        return promise.future();
    }
}