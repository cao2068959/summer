package com.chy.summer.link.client.impl.predicate;

import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.client.predicate.ResponsePredicateResult;
import io.vertx.core.buffer.Buffer;

/**
 * 用于判断HttpResponse的结果断言实现
 */
public class ResponsePredicateResultImpl implements ResponsePredicateResult {

    /**
     * 默认的成功判断的返回对象
     */
    public static final ResponsePredicateResultImpl SUCCESS = new ResponsePredicateResultImpl(true, null);

    /**
     * 是否通过断言
     */
    private final boolean passed;
    /**
     * 错误的时候的详情消息
     */
    private final String message;
    /**
     * 需要进行断言的HttpResponse的持有对象
     */
    private HttpResponse<Buffer> httpResponse;

    public ResponsePredicateResultImpl(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    /**
     * 获取结果是成功还是失败
     *
     * @return {@code true} 断言判断成功, 否则返回{@code false}
     */
    @Override
    public boolean succeeded() {
        return passed;
    }

    /**
     * 获取失败的消息，但有可能是{@code null}.
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * 进行测试的{@link HttpResponse}
     */
    @Override
    public HttpResponse<Buffer> response() {
        return httpResponse;
    }

    public ResponsePredicateResultImpl setHttpResponse(HttpResponse<Buffer> httpResponse) {
        this.httpResponse = httpResponse;
        return this;
    }
}