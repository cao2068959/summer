package com.chy.summer.link.client.impl.predicate;

import com.chy.summer.link.client.predicate.ErrorConverter;
import com.chy.summer.link.client.predicate.ResponsePredicateResult;

import java.util.function.Function;

/**
 * 将ResponsePredicateResult转换为描述错误的Throwable实现
 */
public class ErrorConverterImpl implements ErrorConverter {
    /**
     * 转换器  将ResponsePredicateResult转成Throwable
     */
    private final Function<ResponsePredicateResult, Throwable> converter;
    /**
     * 是否需要body
     */
    private final boolean needsBody;

    public ErrorConverterImpl(Function<ResponsePredicateResult, Throwable> converter, boolean needsBody) {
        this.converter = converter;
        this.needsBody = needsBody;
    }

    /**
     * @return {@code true} 转换器正在处理请求body
     */
    @Override
    public boolean requiresBody() {
        return needsBody;
    }

    /**
     * 执行方法
     */
    @Override
    public Throwable apply(ResponsePredicateResult result) {
        return converter.apply(result);
    }
}