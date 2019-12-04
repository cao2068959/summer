package com.chy.summer.link.client.predicate;

import com.chy.summer.link.client.impl.predicate.ErrorConverterImpl;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.util.function.Function;

/**
 * 将ResponsePredicateResult转换为描述错误的Throwable接口
 */
@FunctionalInterface
public interface ErrorConverter {

    ErrorConverter DEFAULT_CONVERTER = result -> {
        String message = result.message();
        return message == null ? null : new NoStackTraceThrowable(message);
    };

    /**
     * 创建一个完整的ErrorConverter，将通过响应body传递断言的结果
     *
     * 收到HTTP响应的body后，将调用转换器函数
     *
     * @param converter 从ResponsePredicateResult创建Throwable的函数
     */
    static ErrorConverter create(Function<ResponsePredicateResult, Throwable> converter) {
        return converter::apply;
    }

    /**
     * 创建一个完整的ErrorConverter，将与响应的body传递断言的结果.
     *
     * 收到HTTP响应的body后，将调用转换器函数
     *
     * @param converter 从ResponsePredicateResult创建Throwable的函数
     */
    static ErrorConverter createFullBody(Function<ResponsePredicateResult, Throwable> converter) {
        return new ErrorConverterImpl(converter, true);
    }

    Throwable apply(ResponsePredicateResult result);

    /**
     * @return {@code true} 转换器是否有body
     */
    default boolean requiresBody() {
        return false;
    }
}