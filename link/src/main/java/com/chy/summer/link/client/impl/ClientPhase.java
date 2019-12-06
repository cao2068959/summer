package com.chy.summer.link.client.impl;

/**
 * 客户端阶段枚举
 */
public enum ClientPhase {

    /**
     * 尚未创建HttpClientRequest实例，可以完全修改request中的任何配置
     */
    PREPARE_REQUEST,

    /**
     * HttpClientRequest已创建但尚未发送，HTTP方法、URI或请求参数已经无法修改
     */
    SEND_REQUEST,

    /**
     * 已收到HttpClientResponse，但是需要进行重定向
     */
    FOLLOW_REDIRECT,

    /**
     * 已收到HttpClientResponse，并即将创建response
     */
    RECEIVE_RESPONSE,

    /**
     * response已创建完成，即将被分派进行相应的处理
     */
    DISPATCH_RESPONSE,

    /**
     * 发生错误失败了
     */
    FAILURE

}