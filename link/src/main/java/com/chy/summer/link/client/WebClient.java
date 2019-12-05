//package com.chy.summer.link.client;
//
//import io.vertx.core.Vertx;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.http.HttpClient;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.http.RequestOptions;
//import io.vertx.core.http.impl.HttpClientImpl;
//import io.vertx.core.net.SocketAddress;
//
///**
// * 异步的HTTP和HTTP 2.0客户端
// * WebClient会使得Web服务器的HTTP请求/响应交互变得容易，并提供一些高级功能，例如：
// * json类型的body编码与解码
// * 请求/响应的泵（pump）
// * 错误处理
// *
// * WebClient并不是弃用HttpClient，事实上它基于HttpClient，继承了其配置和强大的功能。
// * 如果需要对HTTP请求/响应进行精细的控制，还应当使用HttpClient
// */
//public interface WebClient {
//
//  /**
//   * 使用提供的vertx实例和默认选项创建一个Web客户端
//   *
//   * @param vertx vertx实例
//   * @return 返回创建的Web客户端实例
//   */
//  static WebClient create(Vertx vertx) {
//    WebClientOptions options = new WebClientOptions();
//    return create(vertx, options);
//  }
//
//  /**
//   * 使用提供的vertx实例创建一个Web客户端
//   *
//   * @param vertx   vertx实例
//   * @param options 客户端的WebClientOptions
//   * @return 返回创建的Web客户端实例
//   */
//  static WebClient create(Vertx vertx, WebClientOptions options) {
//    return new WebClientBase(vertx.createHttpClient(options), options);
//  }
//
//  /**
//   * 使用提供的httpClient封装数据，创建Web客户端
//   *
//   * @param httpClient {@link HttpClient}的封装
//   * @return 返回创建的Web客户端实例
//   */
//  static WebClient wrap(HttpClient httpClient) {
//    return wrap(httpClient, new WebClientOptions());
//  }
//
//  /**
//   * 使用提供的httpClient封装数据和WebClientOptions，创建Web客户端
//   *
//   * 仅使用WebClientOptions的Web客户端的部分，其他配置采用httpClient的配置
//   *
//   * @param httpClient {@link HttpClient}的封装
//   * @param options    客户端的WebClientOptions
//   * @return 返回创建的Web客户端实例
//   */
//  static WebClient wrap(HttpClient httpClient, WebClientOptions options) {
//    WebClientOptions actualOptions = new WebClientOptions(((HttpClientImpl) httpClient).getOptions());
//    actualOptions.init(options);
//    return new WebClientBase(httpClient, actualOptions);
//  }
//
//  /**
//   * 创建一个HTTP请求以发送到指定主机和端口上的服务器
//   * @param method  HTTP的method
//   * @param port  端口号
//   * @param host  主机地址
//   * @param requestURI  相对的URI
//   * @return  HTTP的请求对象
//   */
//  HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI);
//
//  /**
//   * 与request(HttpMethod，int，String，String)一样，
//   * 但使用serverAddress参数而不是port和host参数连接到服务器
//   *
//   * 但我们仍需要通过port和host参数创建请求header
//   */
//  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI);
//
//  /**
//   * 创建一个HTTP请求以发送到指定主机和默认端口的服务器
//   * @param method  HTTP的method
//   * @param host  端口号
//   * @param requestURI  相对的URI
//   * @return  HTTP的请求对象
//   */
//  HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI);
//
//  /**
//   * Like {@link #request(HttpMethod, String, String)} using the {@code serverAddress} parameter to connect to the
//   * server instead of the default port and {@code host} parameter.
//   * <p>
//   * The request host header will still be created from the default port and {@code host} parameter.
//   * <p>
//   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
//   */
//  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI);
//
//  /**
//   * Create an HTTP request to send to the server at the default host and port.
//   * @param method  the HTTP method
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> request(HttpMethod method, String requestURI);
//
//  /**
//   * Like {@link #request(HttpMethod, String)} using the {@code serverAddress} parameter to connect to the
//   * server instead of the default port and default host.
//   * <p>
//   * The request host header will still be created from the default port and default host.
//   * <p>
//   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
//   */
//  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI);
//
//  /**
//   * Create an HTTP request to send to the server at the specified host and port.
//   * @param method  the HTTP method
//   * @param options  the request options
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> request(HttpMethod method, RequestOptions options);
//
//  /**
//   * Like {@link #request(HttpMethod, RequestOptions)} using the {@code serverAddress} parameter to connect to the
//   * server instead of the {@code options} parameter.
//   * <p>
//   * The request host header will still be created from the {@code options} parameter.
//   * <p>
//   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
//   */
//  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions options);
//
//  /**
//   * Create an HTTP request to send to the server using an absolute URI
//   * @param method  the HTTP method
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI);
//
//  /**
//   * Like {@link #requestAbs(HttpMethod, String)} using the {@code serverAddress} parameter to connect to the
//   * server instead of the {@code absoluteURI} parameter.
//   * <p>
//   * The request host header will still be created from the {@code absoluteURI} parameter.
//   * <p>
//   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
//   */
//  HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String absoluteURI);
//
//  /**
//   * Create an HTTP GET request to send to the server at the default host and port.
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> get(String requestURI);
//
//  /**
//   * Create an HTTP GET request to send to the server at the specified host and port.
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> get(int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP GET request to send to the server at the specified host and default port.
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> get(String host, String requestURI);
//
//  /**
//   * Create an HTTP GET request to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> getAbs(String absoluteURI);
//
//  /**
//   * Create an HTTP POST request to send to the server at the default host and port.
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> post(String requestURI);
//
//  /**
//   * Create an HTTP POST request to send to the server at the specified host and port.
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> post(int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP POST request to send to the server at the specified host and default port.
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> post(String host, String requestURI);
//
//  /**
//   * Create an HTTP POST request to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> postAbs(String absoluteURI);
//
//  /**
//   * Create an HTTP PUT request to send to the server at the default host and port.
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> put(String requestURI);
//
//  /**
//   * Create an HTTP PUT request to send to the server at the specified host and port.
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> put(int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP PUT request to send to the server at the specified host and default port.
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> put(String host, String requestURI);
//
//  /**
//   * Create an HTTP PUT request to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> putAbs(String absoluteURI);
//
//  /**
//   * Create an HTTP DELETE request to send to the server at the default host and port.
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> delete(String requestURI);
//
//  /**
//   * Create an HTTP DELETE request to send to the server at the specified host and port.
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> delete(int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP DELETE request to send to the server at the specified host and default port.
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> delete(String host, String requestURI);
//
//  /**
//   * Create an HTTP DELETE request to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> deleteAbs(String absoluteURI);
//
//  /**
//   * Create an HTTP PATCH request to send to the server at the default host and port.
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> patch(String requestURI);
//
//  /**
//   * Create an HTTP PATCH request to send to the server at the specified host and port.
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> patch(int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP PATCH request to send to the server at the specified host and default port.
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> patch(String host, String requestURI);
//
//  /**
//   * Create an HTTP PATCH request to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> patchAbs(String absoluteURI);
//
//  /**
//   * Create an HTTP HEAD request to send to the server at the default host and port.
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> head(String requestURI);
//
//  /**
//   * Create an HTTP HEAD request to send to the server at the specified host and port.
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> head(int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP HEAD request to send to the server at the specified host and default port.
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> head(String host, String requestURI);
//
//  /**
//   * Create an HTTP HEAD request to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> headAbs(String absoluteURI);
//
//  /**
//   * Create a request with a custom HTTP method to send to the server at the default host and port.
//   *
//   * @param customHttpMethod custom HTTP Method
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> raw(String customHttpMethod, String requestURI);
//
//  /**
//   * Create a request with a custom HTTP method to send to the server at the specified host and port.
//   *
//   * @param customHttpMethod custom HTTP Method
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> raw(String customHttpMethod, int port, String host, String requestURI);
//
//  /**
//   * Create a request with a custom HTTP method  to send to the server at the specified host and default port.
//   *
//   * @param customHttpMethod custom HTTP Method
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> raw(String customHttpMethod, String host, String requestURI);
//
//  /**
//   * Create a request with a custom HTTP method  to send to the server using an absolute URI, specifying a response handler to receive
//   * the response
//   *
//   * @param customHttpMethod custom HTTP Method
//   * @param absoluteURI  the absolute URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> rawAbs(String customHttpMethod, String absoluteURI);
//
//  /**
//   * Close the client. Closing will close down any pooled connections.
//   * Clients should always be closed after use.
//   */
//  void close();
//}