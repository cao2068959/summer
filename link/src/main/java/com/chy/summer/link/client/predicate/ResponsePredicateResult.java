package com.chy.summer.link.client.predicate;

import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.client.impl.predicate.ResponsePredicateResultImpl;
import javax.annotation.Nullable;
import io.vertx.core.buffer.Buffer;

/**
 * 用于判断HttpResponse的断言结果接口
 */
public interface ResponsePredicateResult {

    /**
     * @return 返回一个成功的标识
     */
    static ResponsePredicateResult success() {
        return ResponsePredicateResultImpl.SUCCESS;
    }

    /**
     * 创建失败的结果
     *
     * @param message 错误详情
     */
    static ResponsePredicateResult failure(String message) {
        return new ResponsePredicateResultImpl(false, message);
    }

    /**
     * 获取结果是成功还是失败
     *
     * @return {@code true} 断言判断成功, 否则返回{@code false}
     */
    boolean succeeded();

    /**
     * 获取失败的消息，但有可能是{@code null}.
     */
    @Nullable
    String message();

    /**
     * 进行测试的{@link HttpResponse}
     */
    @Nullable
    HttpResponse<Buffer> response();
}