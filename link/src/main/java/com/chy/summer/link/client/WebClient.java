package com.chy.summer.link.client;

import com.chy.summer.link.client.impl.WebClientBase;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;

/**
 * 异步的HTTP和HTTP 2.0客户端
 * WebClient会使得Web服务器的HTTP请求/响应交互变得容易，并提供一些高级功能，例如：
 * json类型的body编码与解码
 * 请求/响应的泵（pump）
 * 错误处理
 * <p>
 * WebClient并不是弃用HttpClient，事实上它基于HttpClient，继承了其配置和强大的功能。
 * 如果需要对HTTP请求/响应进行精细的控制，还应当使用HttpClient
 */
public interface WebClient {

    /**
     * 使用提供的vertx实例和默认选项创建一个Web客户端
     *
     * @param vertx vertx实例
     * @return 返回创建的Web客户端实例
     */
    static WebClient create(Vertx vertx) {
        WebClientOptions options = new WebClientOptions();
        return create(vertx, options);
    }

    /**
     * 使用提供的vertx实例创建一个Web客户端
     *
     * @param vertx   vertx实例
     * @param options 客户端的WebClientOptions
     * @return 返回创建的Web客户端实例
     */
    static WebClient create(Vertx vertx, WebClientOptions options) {
        return new WebClientBase(vertx.createHttpClient(options), options);
    }

    /**
     * 使用提供的httpClient封装数据，创建Web客户端
     *
     * @param httpClient {@link HttpClient}的封装
     * @return 返回创建的Web客户端实例
     */
    static WebClient wrap(HttpClient httpClient) {
        return wrap(httpClient, new WebClientOptions());
    }

    /**
     * 使用提供的httpClient封装数据和WebClientOptions，创建Web客户端
     * <p>
     * 仅使用WebClientOptions的Web客户端的部分，其他配置采用httpClient的配置
     *
     * @param httpClient {@link HttpClient}的封装
     * @param options    客户端的WebClientOptions
     * @return 返回创建的Web客户端实例
     */
    static WebClient wrap(HttpClient httpClient, WebClientOptions options) {
        WebClientOptions actualOptions = new WebClientOptions(((HttpClientImpl) httpClient).getOptions());
        actualOptions.init(options);
        return new WebClientBase(httpClient, actualOptions);
    }

    /**
     * 创建一个HTTP请求以发送到指定主机和端口上的服务器
     *
     * @param method     HTTP的method
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对的URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI);

    /**
     * 与request(HttpMethod，int，String，String)一样，
     * 但使用serverAddress参数而不是port和host参数连接到服务器
     * <p>
     * 但我们仍需要通过port和host参数创建请求header
     */
    HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI);

    /**
     * 创建一个HTTP请求以发送到指定主机和默认端口的服务器
     *
     * @param method     HTTP的method
     * @param host       主机地址
     * @param requestURI 相对的URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI);

    /**
     * 与request(HttpMethod，int，String，String)一样，
     * 但使用serverAddress参数而不是默认的host和默认的port参数连接到服务器
     * <p>
     * 但我们仍需要通过host和默认的port参数创建请求header
     */
    HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI);

    /**
     * 创建一个HTTP请求以发送到指定默认主机和默认端口的服务器
     *
     * @param method     HTTP的method
     * @param requestURI 相对的URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> request(HttpMethod method, String requestURI);

    /**
     * 与request(HttpMethod，int，String，String)一样，
     * 但使用serverAddress参数而不是默认的默认的host和默认的port参数连接到服务器
     * <p>
     * 但我们仍需要通过默认的host和默认的port参数创建请求header
     */
    HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI);

    /**
     * 创建一个HTTP请求以发送到指定主机和端口上的服务器
     *
     * @param method  HTTP的method
     * @param options 请求的配置
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> request(HttpMethod method, RequestOptions options);

    /**
     * 与request(HttpMethod，int，String，String)一样，
     * 但使用serverAddress参数而不是port和host参数连接到服务器
     * <p>
     * 但是请求头的创建仍需要RequestOptions
     */
    HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions options);

    /**
     * 创建一个HTTP请求，使用绝对URI发送到服务器
     *
     * @param method      HTTP的method
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI);

    /**
     * 与request(HttpMethod，int，String，String)一样，
     * 但使用serverAddress参数而不是absoluteURI参数连接到服务器
     * <p>
     * 但是请求头的创建仍需要absoluteURI
     */
    HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String absoluteURI);

    /**
     * 创建一个HTTP GET请求发送到默认主机和默认端口的服务器上
     *
     * @param requestURI 相对的URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> get(String requestURI);

    /**
     * 创建一个HTTP GET请求发送到指定的主机和端口的服务器上
     *
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对的URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> get(int port, String host, String requestURI);

    /**
     * 创建一个HTTP GET请求发送到指定的主机和默认的端口的服务器上
     *
     * @param host       主机地址
     * @param requestURI 相对的URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> get(String host, String requestURI);

    /**
     * 创建一个HTTP GET请求使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> getAbs(String absoluteURI);

    /**
     * 创建一个HTTP POST请求发送到默认主机和默认端口的服务器
     *
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> post(String requestURI);

    /**
     * 创建一个HTTP POST请求发送到指定的主机和指定的端口的服务器
     *
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> post(int port, String host, String requestURI);

    /**
     * 创建一个HTTP POST请求，发送到指定主机和默认端口的服务器
     *
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> post(String host, String requestURI);

    /**
     * 创建一个HTTP POST请求使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> postAbs(String absoluteURI);

    /**
     * 创建一个HTTP PUT请求，发送到默认主机和默认端口的服务器
     *
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> put(String requestURI);

    /**
     * 创建一个HTTP PUT请求，发送到指定主机和指定端口的服务器
     *
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> put(int port, String host, String requestURI);

    /**
     * 创建一个HTTP PUT请求，发送到指定主机和默认端口的服务器
     *
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> put(String host, String requestURI);

    /**
     * 创建一个HTTP PUT请求使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> putAbs(String absoluteURI);

    /**
     * 创建一个HTTP DELETE请求，发送到默认主机和默认端口的服务器
     *
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> delete(String requestURI);

    /**
     * 创建一个HTTP DELETE请求，发送到指定主机和指定端口的服务器
     *
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> delete(int port, String host, String requestURI);

    /**
     * 创建一个HTTP DELETE请求，发送到指定主机和默认端口的服务器
     *
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> delete(String host, String requestURI);

    /**
     * 创建一个HTTP DELETE请求使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> deleteAbs(String absoluteURI);

    /**
     * 创建一个HTTP PATCH请求，发送到默认主机和默认端口的服务器
     *
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> patch(String requestURI);

    /**
     * 创建一个HTTP PATCH请求，发送到指定主机和指定端口的服务器
     *
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> patch(int port, String host, String requestURI);

    /**
     * 创建一个HTTP PATCH请求，发送到指定主机和默认端口的服务器
     *
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> patch(String host, String requestURI);

    /**
     * 创建一个HTTP PATCH请求使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> patchAbs(String absoluteURI);

    /**
     * 创建一个HTTP HEAD请求，发送到默认主机和默认端口的服务器
     *
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> head(String requestURI);

    /**
     * 创建一个HTTP PATCH请求，发送到指定主机和指定端口的服务器
     *
     * @param port       端口号
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> head(int port, String host, String requestURI);

    /**
     * 创建一个HTTP PATCH请求，发送到指定主机和默认端口的服务器
     *
     * @param host       主机地址
     * @param requestURI 相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> head(String host, String requestURI);

    /**
     * 创建一个HTTP HEAD请求使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param absoluteURI 绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> headAbs(String absoluteURI);

    /**
     * 使用自定义HTTP方法创建一个请求，发送到默认主机和默认端口的服务器
     *
     * @param customHttpMethod 自定义的HTTP Method
     * @param requestURI       相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> raw(String customHttpMethod, String requestURI);

    /**
     * 使用自定义HTTP方法创建一个请求，发送到指定主机和指定端口的服务器
     *
     * @param customHttpMethod 自定义的HTTP Method
     * @param port             端口号
     * @param host             主机地址
     * @param requestURI       相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> raw(String customHttpMethod, int port, String host, String requestURI);

    /**
     * 使用自定义HTTP方法创建一个请求，发送到指定主机和默认端口的服务器
     *
     * @param customHttpMethod 自定义的HTTP Method
     * @param host             主机地址
     * @param requestURI       相对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> raw(String customHttpMethod, String host, String requestURI);

    /**
     * 使用自定义HTTP方法创建一个请求，使用绝对URI发送到服务器，并指定一个响应处理程序接收响应
     *
     * @param customHttpMethod 自定义的HTTP Method
     * @param absoluteURI      绝对URI
     * @return HTTP的请求对象
     */
    HttpRequest<Buffer> rawAbs(String customHttpMethod, String absoluteURI);

    /**
     * 关闭客户端,将关闭所有的连接
     * 每次使用后都应该关闭客户端
     */
    void close();
}