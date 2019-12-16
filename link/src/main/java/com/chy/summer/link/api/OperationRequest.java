package com.chy.summer.link.api;

import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class OperationRequest {

  /**
   * 请求将参数解析为JSON
   */
  private JsonObject params;
  /**
   * 请求头
   */
  private MultiMap headers;
  /**
   * 如果用户进行身份验证,就会包含routingContext.user().principal()
   */
  private JsonObject user;

  /**
   * 额外的信息
   */
  private JsonObject extra;

  public OperationRequest() {
    init();
  }

  public OperationRequest(JsonObject json) {
    init();
    OperationRequestConverter.fromJson(json, this);
    JsonObject hdrs = json.getJsonObject("headers", null);
    if (hdrs != null) {
      headers = new CaseInsensitiveHeaders();
      for (Map.Entry<String, Object> entry: hdrs) {
        if (!(entry.getValue() instanceof String)) {
          throw new IllegalStateException("请求头值的类型无效 " + entry.getValue().getClass());
        }
        headers.set(entry.getKey(), (String)entry.getValue());
      }
    }
  }

  public OperationRequest(JsonObject params, MultiMap headers, JsonObject user, JsonObject extra) {
    this.params = params;
    this.headers = headers;
    this.user = user;
    this.extra = extra;
  }

  public OperationRequest(OperationRequest other) {
    this.params = other.getParams();
    this.headers = other.getHeaders();
    this.user = other.getUser();
    this.extra = other.getExtra();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    OperationRequestConverter.toJson(this, json);
    if (headers != null) {
      JsonObject hJson = new JsonObject();
      headers.entries().forEach(entry -> hJson.put(entry.getKey(), entry.getValue()));
      json.put("headers", hJson);
    }
    return json;
  }

  /**
   * 初始化
   */
  private void init() {
    this.params = new JsonObject();
    this.headers = MultiMap.caseInsensitiveMultiMap();
    this.user = null;
    this.extra = null;
  }

  /**
   * 获取请求参数
   */
  public JsonObject getParams() {
    return params;
  }

  /**
   * 获取请求头
   */
  public MultiMap getHeaders() {
    return headers;
  }

  /**
   * 获取用户信息
   */
  public JsonObject getUser() { return user; }

  /**
   * 获取额外的信息
   */
  public JsonObject getExtra() {
    return extra;
  }

  public OperationRequest setParams(JsonObject params) {
    this.params = params;
    return this;
  }

  public OperationRequest setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  public OperationRequest setUser(JsonObject user) {
    this.user = user;
    return this;
  }

  public OperationRequest setExtra(JsonObject extra) {
    this.extra = extra;
    return this;
  }
}