package com.chy.summer.link.client.impl.predicate;

import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.client.predicate.ErrorConverter;
import com.chy.summer.link.client.predicate.ResponsePredicate;
import com.chy.summer.link.client.predicate.ResponsePredicateResult;

import java.util.function.Function;

/**
 * HttpResponse的断言实现
 */
public class ResponsePredicateImpl implements ResponsePredicate {

    /**
     * 断言
     */
    private final Function<HttpResponse<Void>, ResponsePredicateResult> predicate;
    /**
     * 错误转换器
     */
    private final ErrorConverter errorConverter;

    public ResponsePredicateImpl(Function<HttpResponse<Void>, ResponsePredicateResult> predicate, ErrorConverter errorConverter) {
        this.predicate = predicate;
        this.errorConverter = errorConverter;
    }

    /**
     * 执行方法
     */
    @Override
    public ResponsePredicateResult apply(HttpResponse<Void> response) {
        return predicate.apply(response);
    }

    /**
     * @return 当前使用的错误转换器
     */
    @Override
    public ErrorConverter errorConverter() {
        return errorConverter;
    }

}