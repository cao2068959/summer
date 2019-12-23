package com.chy.summer.link.api.contract;

import io.vertx.core.json.JsonObject;

public class RouterFactoryOptions {

  /**
   * 失败验证处理器
   * 默认情况下，RouterFactory不使用失败验证处理器
   * RouterFactory不管理错误验证
   */
  @Deprecated
  public final static boolean DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER = false;

  /**
   * 未执行的处理程序
   * 默认情况下，RouterFactory会处理“未实现” /“不允许的方法”的方法
   */
  public final static boolean DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER = true;

  /**
   * 安全处理器
   * 默认情况下，RouterFactory要求在调用getRouter()时定义安全处理器，否则它将引发异常
   */
  public final static boolean DEFAULT_REQUIRE_SECURITY_HANDLERS = true;

  /**
   * 响应内容类型处理器
   * 默认情况下，RouterFactory在需要时配置响应内容类型处理程序
   */
  public final static boolean DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER = true;

  /**
   * 默认情况下，RouterFactory不会在路由上下文中公开操作配置
   */
  public final static String DEFAULT_OPERATION_MODEL_KEY = null;

  private boolean mountValidationFailureHandler;
  private boolean mountNotImplementedHandler;
  private boolean requireSecurityHandlers;
  private boolean mountResponseContentTypeHandler;
  private String operationModelKey;

  public RouterFactoryOptions() {
    init();
  }

  public RouterFactoryOptions(JsonObject json) {
    init();
    RouterFactoryOptionsConverter.fromJson(json, this);
  }

  public RouterFactoryOptions(RouterFactoryOptions other) {
    this.mountValidationFailureHandler = other.isMountValidationFailureHandler();
    this.mountNotImplementedHandler = other.isMountNotImplementedHandler();
    this.requireSecurityHandlers = other.isRequireSecurityHandlers();
    this.mountResponseContentTypeHandler = other.isMountResponseContentTypeHandler();
    this.operationModelKey = other.getOperationModelKey();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RouterFactoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.mountValidationFailureHandler = DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER;
    this.mountNotImplementedHandler = DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER;
    this.requireSecurityHandlers = DEFAULT_REQUIRE_SECURITY_HANDLERS;
    this.mountResponseContentTypeHandler = DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER;
    this.operationModelKey = DEFAULT_OPERATION_MODEL_KEY;
  }

  /**
   * @deprecated Router Factory不会管理错误校验，请使用{@link Router#errorHandler(int, Handler)}
   * @return
   */
  @Deprecated
  public boolean isMountValidationFailureHandler() {
    return mountValidationFailureHandler;
  }

  /**
   * 启用或禁用失败验证处理程序
   * 如果在路由的创建过程中启用了它，则会配置一个ValidationException的故障处理器。
   * 可以使用将调用函数111的处理程序来更改验证失败处理程序。
   * 可以使用 {@link RouterFactory#setValidationFailureHandler(Handler)}来更改失败校验处理器。
   * 如果失败的不是ValidationException，则将调用下一个失败处理程序。
   *
   * @param mountGlobalValidationFailureHandler
   */
  @Deprecated
  public RouterFactoryOptions setMountValidationFailureHandler(boolean mountGlobalValidationFailureHandler) {
    this.mountValidationFailureHandler = mountGlobalValidationFailureHandler;
    return this;
  }

  public boolean isMountNotImplementedHandler() {
    return mountNotImplementedHandler;
  }

  /**
   * true：则RouterFactory将自动配置一个处理程序，用于对未指定处理程序的每个操作返回HTTP 405/501状态代码。
   * 可以使用方法{@link io.vertx.ext.web.Router#errorHandler(int, Handler)}自定义响应
   *
   * @param mountOperationsWithoutHandler
   */
  public RouterFactoryOptions setMountNotImplementedHandler(boolean mountOperationsWithoutHandler) {
    this.mountNotImplementedHandler = mountOperationsWithoutHandler;
    return this;
  }

  public boolean isRequireSecurityHandlers() {
    return requireSecurityHandlers;
  }

  /**
   * 如果为true，那么调用{@link RouterFactory#getRouter()}方法的时候，
   * RouterFactory会为每个路径配置所需的安全处理器，如果未定义安全处理程序，则会抛出RouterFactoryException
   *
   * @param requireSecurityHandlers
   * @return this object
   */
  public RouterFactoryOptions setRequireSecurityHandlers(boolean requireSecurityHandlers) {
    this.requireSecurityHandlers = requireSecurityHandlers;
    return this;
  }

  public boolean isMountResponseContentTypeHandler() {
    return mountResponseContentTypeHandler;
  }

  /**
   * 如果为true，则在需要时，RouterFactory会配置{@link io.vertx.ext.web.handler.ResponseContentTypeHandler}
   * @param mountResponseContentTypeHandler
   * @return
   */
  public RouterFactoryOptions setMountResponseContentTypeHandler(boolean mountResponseContentTypeHandler) {
    this.mountResponseContentTypeHandler = mountResponseContentTypeHandler;
    return this;
  }

  public String getOperationModelKey() {
    return operationModelKey;
  }

  /**
   * 设置一个附加的处理器，用于在给定route上下文中直接处理模型
   * @param operationModelKey
   */
  public RouterFactoryOptions setOperationModelKey(String operationModelKey) {
    this.operationModelKey = operationModelKey;
    return this;
  }
}