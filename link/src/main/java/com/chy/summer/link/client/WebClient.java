//package com.chy.summer.link.client;
//
//import io.vertx.core.Vertx;
//
//public interface WebClient {
//
//  /**
//   * Create a web client using the provided {@code vertx} instance and default options.
//   *
//   * @param vertx the vertx instance
//   * @return the created web client
//   */
//  static WebClient create(Vertx vertx) {
//    WebClientOptions options = new WebClientOptions();
//    return create(vertx, options);
//  }
//
//  /**
//   * Create a web client using the provided {@code vertx} instance.
//   *
//   * @param vertx   the vertx instance
//   * @param options the Web Client options
//   * @return the created web client
//   */
//  static WebClient create(Vertx vertx, WebClientOptions options) {
//    return new WebClientBase(vertx.createHttpClient(options), options);
//  }
//
//  /**
//   * Wrap an {@code httpClient} with a web client and default options.
//   *
//   * @param httpClient the {@link HttpClient} to wrap
//   * @return the web client
//   */
//  static WebClient wrap(HttpClient httpClient) {
//    return wrap(httpClient, new WebClientOptions());
//  }
//
//  /**
//   * Wrap an {@code httpClient} with a web client and default options.
//   * <p>
//   * Only the specific web client portion of the {@code options} is used, the {@link io.vertx.core.http.HttpClientOptions}
//   * of the {@code httpClient} is reused.
//   *
//   * @param httpClient the {@link HttpClient} to wrap
//   * @param options    the Web Client options
//   * @return the web client
//   */
//  static WebClient wrap(HttpClient httpClient, WebClientOptions options) {
//    WebClientOptions actualOptions = new WebClientOptions(((HttpClientImpl) httpClient).getOptions());
//    actualOptions.init(options);
//    return new WebClientBase(httpClient, actualOptions);
//  }
//
//  /**
//   * Create an HTTP request to send to the server at the specified host and port.
//   * @param method  the HTTP method
//   * @param port  the port
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
//   */
//  HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI);
//
//  /**
//   * Like {@link #request(HttpMethod, int, String, String)} using the {@code serverAddress} parameter to connect to the
//   * server instead of the {@code port} and {@code host} parameters.
//   * <p>
//   * The request host header will still be created from the {@code port} and {@code host} parameters.
//   * <p>
//   * Use {@link SocketAddress#domainSocketAddress(String)} to connect to a unix domain socket server.
//   */
//  HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI);
//
//  /**
//   * Create an HTTP request to send to the server at the specified host and default port.
//   * @param method  the HTTP method
//   * @param host  the host
//   * @param requestURI  the relative URI
//   * @return  an HTTP client request object
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