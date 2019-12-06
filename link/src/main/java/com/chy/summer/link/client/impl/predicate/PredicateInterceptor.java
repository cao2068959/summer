package com.chy.summer.link.client.impl.predicate;

import com.chy.summer.link.client.impl.ClientPhase;
import com.chy.summer.link.client.impl.HttpContext;
import com.chy.summer.link.client.impl.HttpRequestImpl;
import com.chy.summer.link.client.impl.HttpResponseImpl;
import com.chy.summer.link.client.predicate.ErrorConverter;
import com.chy.summer.link.client.predicate.ResponsePredicate;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.util.ArrayList;
import java.util.List;

/**
 * 断言拦截器
 */
public class PredicateInterceptor implements Handler<HttpContext<?>> {

    @Override
    public void handle(HttpContext<?> httpContext) {

        if (httpContext.phase() == ClientPhase.RECEIVE_RESPONSE) {

            //获取请求实例
            HttpRequestImpl request = (HttpRequestImpl) httpContext.request();
            //获取响应
            HttpClientResponse resp = httpContext.clientResponse();
            //获取请求里所有的断言
            List<ResponsePredicate> expectations = request.expectations;
            if (expectations != null) {
                for (ResponsePredicate expectation : expectations) {
                    //断言结果声明
                    ResponsePredicateResultImpl predicateResult;
                    try {
                        //执行断言
                        predicateResult = (ResponsePredicateResultImpl) expectation.apply(responseCopy(resp, httpContext, null));
                    } catch (Exception e) {
                        //上下文中写入失败信息
                        httpContext.fail(e);
                        return;
                    }
                    if (!predicateResult.succeeded()) {
                        //断言校验失败，获取错误信息
                        ErrorConverter errorConverter = expectation.errorConverter();
                        if (!errorConverter.requiresBody()) {
                            //记录错误
                            failOnPredicate(httpContext, errorConverter, predicateResult);
                        } else {
                            resp.bodyHandler(buffer -> {
                                //将响应信息记录到断言结果中
                                predicateResult.setHttpResponse(responseCopy(resp, httpContext, buffer));
                                failOnPredicate(httpContext, errorConverter, predicateResult);
                            });
                            resp.resume();
                        }
                        return;
                    }
                }
            }
        }

        httpContext.next();
    }

    /**
     * 拷贝响应信息
     *
     * @param resp        HTTP响应
     * @param httpContext HTTP山下文
     * @param value       body中的结果
     */
    private <B> HttpResponseImpl<B> responseCopy(HttpClientResponse resp, HttpContext<?> httpContext, B value) {
        //创建信息的对象副本，防止原对象修改影响到副本
        return new HttpResponseImpl<>(
                resp.version(),
                resp.statusCode(),
                resp.statusMessage(),
                MultiMap.caseInsensitiveMultiMap().addAll(resp.headers()),
                null,
                new ArrayList<>(resp.cookies()),
                value,
                httpContext.getRedirectedLocations()
        );
    }

    /**
     * 错误信息在ResponsePredicateResultImpl中，
     * 将错误信息通过ErrorConverter转换成Throwable
     *
     * @param ctx             Http上下文
     * @param converter       错误转换器
     * @param predicateResult 请求结果的断言结果
     */
    private void failOnPredicate(HttpContext<?> ctx, ErrorConverter converter, ResponsePredicateResultImpl predicateResult) {
        Throwable result;
        try {
            //将predicateResult转成Throwable，获取错误信息
            result = converter.apply(predicateResult);
        } catch (Exception e) {
            result = e;
        }
        if (result != null) {
            //写入错误信息
            ctx.fail(result);
        } else {
            //写入错误信息
            ctx.fail(new NoStackTraceThrowable("无效的HTTP响应"));
        }
    }
}