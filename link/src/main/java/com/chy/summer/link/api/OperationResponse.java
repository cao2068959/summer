package com.chy.summer.link.api;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class OperationResponse {

  private final static Integer DEFAULT_STATUS_CODE = 200;

  private Integer statusCode;
  private String statusMessage;
  private Buffer payload;
  private MultiMap headers;

  public OperationResponse() {
    init();
  }

  public OperationResponse(JsonObject json) {
    init();
    OperationResponseConverter.fromJson(json, this);
    JsonObject hdrs = json.getJsonObject("headers", null);
    if (hdrs != null) {
      headers = new CaseInsensitiveHeaders();
      for (Map.Entry<String, Object> entry: hdrs) {
        if (!(entry.getValue() instanceof String)) {
          throw new IllegalStateException("请求头的类型无效" + entry.getValue().getClass());
        }
        headers.set(entry.getKey(), (String)entry.getValue());
      }
    }
  }

  public OperationResponse(Integer statusCode, String statusMessage, Buffer payload, MultiMap headers) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.payload = payload;
    this.headers = headers;
  }

  public OperationResponse(OperationResponse other) {
    this.statusCode = other.getStatusCode();
    this.statusMessage = other.getStatusMessage();
    this.payload = other.getPayload();
    this.headers = other.getHeaders();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    OperationResponseConverter.toJson(this, json);
    if (headers != null) {
      JsonObject hJson = new JsonObject();
      headers.entries().forEach(entry -> hJson.put(entry.getKey(), entry.getValue()));
      json.put("headers", hJson);
    }
    return json;
  }

  private void init() {
    this.statusCode = DEFAULT_STATUS_CODE;
    this.payload = null;
    this.headers = MultiMap.caseInsensitiveMultiMap();
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MultiMap getHeaders() {
    return headers;
  }

  public OperationResponse setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  public OperationResponse setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public OperationResponse setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
    return this;
  }

  public OperationResponse setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  public OperationResponse putHeader(String key, String value) {
    this.headers.add(key, value);
    return this;
  }

  public static OperationResponse completedWithJson(JsonObject jsonObject) {
    return completedWithJson(jsonObject.toBuffer());
  }

  public static OperationResponse completedWithJson(JsonArray jsonArray) {
   return completedWithJson(jsonArray.toBuffer());
  }

  /**
   * 创建完成的Json响应
   * @param json
   */
  public static OperationResponse completedWithJson(Buffer json) {
    OperationResponse op = new OperationResponse();
    op.setStatusCode(200);
    op.setStatusMessage("OK");
    op.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json");
    op.setPayload(json);
    return op;
  }

  /**
   * 创建完成的text响应
   * @param text
   */
  public static OperationResponse completedWithPlainText(Buffer text) {
    return new OperationResponse()
      .setStatusCode(200)
      .setStatusMessage("OK")
      .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
      .setPayload(text);
  }
}