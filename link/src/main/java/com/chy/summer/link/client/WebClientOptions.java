package com.chy.summer.link.client;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.impl.launcher.commands.VersionCommand;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * web客户端配置
 */
public class WebClientOptions extends HttpClientOptions {

  /**
   * Web客户端是否应发送用User-Agent的默认值
   * 默认值为true
   */
  public static final boolean DEFAULT_USER_AGENT_ENABLED = true;

  /**
   * User-Agent的默认值
   * 默认值为Vert.x-WebClient的版本
   */
  public static final String DEFAULT_USER_AGENT = loadUserAgent();

  /**
   * Web客户端是否应遵循重定向的默认值
   * 默认值为true
   */
  public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

  /**
   * Web客户端是否应发送用User-Agent
   */
  private boolean userAgentEnabled = DEFAULT_USER_AGENT_ENABLED;
  /**
   * User-Agent值
   */
  private String userAgent = DEFAULT_USER_AGENT;
  /**
   * Web客户端是否应遵循重定向
   */
  private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;

  public WebClientOptions() {
  }

  /**
   * 复制用的构造器
   *
   * @param other 需要复制的配置
   */
  public WebClientOptions(WebClientOptions other) {
    super(other);
    init(other);
  }

  /**
   * 复制{@link HttpClientOptions}的配置
   *
   * @param other 需要复制的配置
   */
  public WebClientOptions(HttpClientOptions other) {
    super(other);
  }

  /**
   * 根据JSON创建一个新实例
   *
   * @param json JSON对象
   */
  public WebClientOptions(JsonObject json) {
    super(json);
    WebClientOptionsConverter.fromJson(json, this);
  }

  /**
   * 初始化
   */
  void init(WebClientOptions other) {
    this.userAgentEnabled = other.userAgentEnabled;
    this.userAgent = other.userAgent;
    this.followRedirects = other.followRedirects;
  }

  /**
   * 转换为JSON
   *
   * @return 返回json对象
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    WebClientOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * @return 如果Web客户端应发送User-Agent，则为true，否则为false
   */
  public boolean isUserAgentEnabled() {
    return userAgentEnabled;
  }

  /**
   * 设置Web客户端是否应发送User-Agent  默认为true
   *
   * @param userAgentEnabled 发送User-Agent为true，否则为false
   * @return 返回当前配置的对象，用于链式调用
   */
  public WebClientOptions setUserAgentEnabled(boolean userAgentEnabled) {
    this.userAgentEnabled = userAgentEnabled;
    return this;
  }

  /**
   * @return 返回User-Agent值
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * 设置User-Agent的值，默认值为Vert.x-WebClient的版本
   *
   * @param userAgent User-Agent的值
   * @return 返回当前配置的对象，用于链式调用
   */
  public WebClientOptions setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  /**
   * @return 是否遵循3XX的代码进行重定向，默认为true
   */
  public boolean isFollowRedirects() {
    return followRedirects;
  }

  /**
   * 设置是否遵循3XX的代码进行重定向，默认为true
   *
   * @param followRedirects 如果重定向，则为true，否则为false
   * @return 返回当前配置的对象，用于链式调用
   */
  public WebClientOptions setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  @Override
  public WebClientOptions setMaxRedirects(int maxRedirects) {
    return (WebClientOptions) super.setMaxRedirects(maxRedirects);
  }

  @Override
  public WebClientOptions setSendBufferSize(int sendBufferSize) {
    return (WebClientOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public WebClientOptions setReceiveBufferSize(int receiveBufferSize) {
    return (WebClientOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public WebClientOptions setReuseAddress(boolean reuseAddress) {
    return (WebClientOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public WebClientOptions setTrafficClass(int trafficClass) {
    return (WebClientOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public WebClientOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (WebClientOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public WebClientOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (WebClientOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public WebClientOptions setSoLinger(int soLinger) {
    return (WebClientOptions) super.setSoLinger(soLinger);
  }

  @Override
  public WebClientOptions setIdleTimeout(int idleTimeout) {
    return (WebClientOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    return (WebClientOptions) super.setIdleTimeoutUnit(idleTimeoutUnit);
  }

  @Override
  public WebClientOptions setSsl(boolean ssl) {
    return (WebClientOptions) super.setSsl(ssl);
  }

  @Override
  public WebClientOptions setKeyCertOptions(KeyCertOptions options) {
    return (WebClientOptions) super.setKeyCertOptions(options);
  }

  @Override
  public WebClientOptions setKeyStoreOptions(JksOptions options) {
    return (WebClientOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public WebClientOptions setPfxKeyCertOptions(PfxOptions options) {
    return (WebClientOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public WebClientOptions setTrustOptions(TrustOptions options) {
    return (WebClientOptions) super.setTrustOptions(options);
  }

  @Override
  public WebClientOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (WebClientOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public WebClientOptions setTrustStoreOptions(JksOptions options) {
    return (WebClientOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public WebClientOptions setPfxTrustOptions(PfxOptions options) {
    return (WebClientOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public WebClientOptions setPemTrustOptions(PemTrustOptions options) {
    return (WebClientOptions) super.setPemTrustOptions(options);
  }

  @Override
  public WebClientOptions addEnabledCipherSuite(String suite) {
    return (WebClientOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public WebClientOptions addCrlPath(String crlPath) throws NullPointerException {
    return (WebClientOptions) super.addCrlPath(crlPath);
  }

  @Override
  public WebClientOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (WebClientOptions) super.addCrlValue(crlValue);
  }

  @Override
  public WebClientOptions setConnectTimeout(int connectTimeout) {
    return (WebClientOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public WebClientOptions setTrustAll(boolean trustAll) {
    return (WebClientOptions) super.setTrustAll(trustAll);
  }

  @Override
  public WebClientOptions setMaxPoolSize(int maxPoolSize) {
    return (WebClientOptions) super.setMaxPoolSize(maxPoolSize);
  }

  @Override
  public WebClientOptions setHttp2MultiplexingLimit(int limit) {
    return (WebClientOptions) super.setHttp2MultiplexingLimit(limit);
  }

  @Override
  public WebClientOptions setHttp2MaxPoolSize(int max) {
    return (WebClientOptions) super.setHttp2MaxPoolSize(max);
  }

  @Override
  public WebClientOptions setHttp2ConnectionWindowSize(int http2ConnectionWindowSize) {
    return (WebClientOptions) super.setHttp2ConnectionWindowSize(http2ConnectionWindowSize);
  }

  @Override
  public WebClientOptions setKeepAlive(boolean keepAlive) {
    return (WebClientOptions) super.setKeepAlive(keepAlive);
  }

  @Override
  public WebClientOptions setPipelining(boolean pipelining) {
    return (WebClientOptions) super.setPipelining(pipelining);
  }

  @Override
  public WebClientOptions setPipeliningLimit(int limit) {
    return (WebClientOptions) super.setPipeliningLimit(limit);
  }

  @Override
  public WebClientOptions setVerifyHost(boolean verifyHost) {
    return (WebClientOptions) super.setVerifyHost(verifyHost);
  }

  @Override
  public WebClientOptions setTryUseCompression(boolean tryUseCompression) {
    return (WebClientOptions) super.setTryUseCompression(tryUseCompression);
  }

  @Override
  public WebClientOptions setSendUnmaskedFrames(boolean sendUnmaskedFrames) {
    return (WebClientOptions) super.setSendUnmaskedFrames(sendUnmaskedFrames);
  }

  @Override
  public WebClientOptions setMaxWebsocketFrameSize(int maxWebsocketFrameSize) {
    return (WebClientOptions) super.setMaxWebsocketFrameSize(maxWebsocketFrameSize);
  }

  @Override
  public WebClientOptions setDefaultHost(String defaultHost) {
    return (WebClientOptions) super.setDefaultHost(defaultHost);
  }

  @Override
  public WebClientOptions setDefaultPort(int defaultPort) {
    return (WebClientOptions) super.setDefaultPort(defaultPort);
  }

  @Override
  public WebClientOptions setMaxChunkSize(int maxChunkSize) {
    return (WebClientOptions) super.setMaxChunkSize(maxChunkSize);
  }

  @Override
  public WebClientOptions setProtocolVersion(HttpVersion protocolVersion) {
    return (WebClientOptions) super.setProtocolVersion(protocolVersion);
  }

  @Override
  public WebClientOptions setMaxHeaderSize(int maxHeaderSize) {
    return (WebClientOptions) super.setMaxHeaderSize(maxHeaderSize);
  }

  @Override
  public WebClientOptions setMaxWaitQueueSize(int maxWaitQueueSize) {
    return (WebClientOptions) super.setMaxWaitQueueSize(maxWaitQueueSize);
  }

  @Override
  public WebClientOptions setUseAlpn(boolean useAlpn) {
    return (WebClientOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public WebClientOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (WebClientOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public WebClientOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (WebClientOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public WebClientOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (WebClientOptions) super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public WebClientOptions setHttp2ClearTextUpgrade(boolean value) {
    return (WebClientOptions) super.setHttp2ClearTextUpgrade(value);
  }

  @Override
  public WebClientOptions setAlpnVersions(List<HttpVersion> alpnVersions) {
    return (WebClientOptions) super.setAlpnVersions(alpnVersions);
  }

  @Override
  public WebClientOptions setMetricsName(String metricsName) {
    return (WebClientOptions) super.setMetricsName(metricsName);
  }

  @Override
  public WebClientOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (WebClientOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public WebClientOptions setLocalAddress(String localAddress) {
    return (WebClientOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public WebClientOptions setLogActivity(boolean logEnabled) {
    return (WebClientOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public WebClientOptions addEnabledSecureTransportProtocol(String protocol) {
    return (WebClientOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public WebClientOptions removeEnabledSecureTransportProtocol(String protocol) {
    return (WebClientOptions) super.removeEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public WebClientOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (WebClientOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  @Override
  public WebClientOptions setReusePort(boolean reusePort) {
    return (WebClientOptions) super.setReusePort(reusePort);
  }

  @Override
  public WebClientOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (WebClientOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public WebClientOptions setTcpCork(boolean tcpCork) {
    return (WebClientOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public WebClientOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (WebClientOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public WebClientOptions setHttp2KeepAliveTimeout(int keepAliveTimeout) {
    return (WebClientOptions) super.setHttp2KeepAliveTimeout(keepAliveTimeout);
  }

  @Override
  public WebClientOptions setForceSni(boolean forceSni) {
    return (WebClientOptions) super.setForceSni(forceSni);
  }

  @Override
  public WebClientOptions setDecoderInitialBufferSize(int decoderInitialBufferSize) {
    return (WebClientOptions) super.setDecoderInitialBufferSize(decoderInitialBufferSize);
  }

  @Override
  public WebClientOptions setPoolCleanerPeriod(int poolCleanerPeriod) {
    return (WebClientOptions) super.setPoolCleanerPeriod(poolCleanerPeriod);
  }

  @Override
  public WebClientOptions setKeepAliveTimeout(int keepAliveTimeout) {
    return (WebClientOptions) super.setKeepAliveTimeout(keepAliveTimeout);
  }

  @Override
  public WebClientOptions setMaxWebsocketMessageSize(int maxWebsocketMessageSize) {
    return (WebClientOptions) super.setMaxWebsocketMessageSize(maxWebsocketMessageSize);
  }

  @Override
  public WebClientOptions setMaxInitialLineLength(int maxInitialLineLength) {
    return (WebClientOptions) super.setMaxInitialLineLength(maxInitialLineLength);
  }

  @Override
  public WebClientOptions setInitialSettings(Http2Settings settings) {
    return (WebClientOptions) super.setInitialSettings(settings);
  }



  public static String loadUserAgent() {
    String userAgent = "Vert.x-WebClient";
    String version = VersionCommand.getVersion();
    if (version.length() > 0) {
      userAgent += "/" + version;
    }
    return userAgent;
  }
}