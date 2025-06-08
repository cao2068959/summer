package com.chy.summer.link.client;

import com.chy.summer.link.common.codec.BodyCodec;
import com.chy.summer.link.common.codec.impl.BodyCodecImpl;
import javax.annotation.Nullable;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 一个HTTP的响应
 * 常见的HTTP响应包含以下属性
 * {@link #statusCode()} HTTP的状态码
 * {@link #statusMessage()} HTTP的状态消息
 * {@link #headers()} HTTP的头信息
 * {@link #version()} HTTP的版本
 * 响应的正文信息，在body中携带，
 * 调用body()方法可以将信息解密构建为指定的{@link BodyCodec}格式并返回
 * <p>
 * 注意：如果使用这个HttpResponse将会强制完全缓存响应的body，并将其存放在内存中
 *
 * @param <T>
 */
public interface HttpResponse<T> {

    /**
     * @return 返回HTTP的版本
     */
    HttpVersion version();

    /**
     * @return 返回HTTP的状态码
     */
    int statusCode();

    /**
     * @return 返回HTTP的状态消息
     */
    String statusMessage();

    /**
     * @return 返回HTTP的头信息
     */
    MultiMap headers();

    /**
     * 根据指定的名称查询HTTP的头信息中第一个值
     *
     * @param headerName 头信息名称
     * @return 头信息值
     */
    @Nullable
    String getHeader(String headerName);

    /**
     * @return 返回响应的trailers
     */
    MultiMap trailers();

    /**
     * 根据指定的名称查询trailers中第一个值
     *
     * @param trailerName trailer名称
     * @return 返回trailers值
     */
    @Nullable
    String getTrailer(String trailerName);

    /**
     * @return 获取设置的Cookie headers（包括trailers）
     */
    List<String> cookies();

    /**
     * @return 返回响应主体，格式为已解码
     */
    @Nullable
    T body();

    /**
     * @return 返回响应主体，将其解码成Buffer类型，如果使用buffer以外的解码器则返回null
     */
    @Nullable
    Buffer bodyAsBuffer();

    /**
     * @return 获取重定向的列表，包括最终位置
     */
    List<String> followedRedirects();

    /**
     * @return 返回响应主体，将其解码成String类型，如果使用buffer以外的解码器则返回null
     */
    @Nullable
    default String bodyAsString() {
        Buffer b = bodyAsBuffer();
        return b != null ? BodyCodecImpl.UTF8_DECODER.apply(b) : null;
    }

    /**
     * @return 返回响应主体，使用指定的编码将其解码成String类型，如果使用buffer以外的解码器则返回null
     */
    @Nullable
    default String bodyAsString(String encoding) {
        Buffer b = bodyAsBuffer();
        return b != null ? b.toString(encoding) : null;
    }

    /**
     * @return 返回响应主体，将其解码成Json类型，如果使用buffer以外的解码器则返回null
     */
    @Nullable
    default JsonObject bodyAsJsonObject() {
        Buffer b = bodyAsBuffer();
        return b != null ? BodyCodecImpl.JSON_OBJECT_DECODER.apply(b) : null;
    }

    /**
     * @return 返回响应主体，将其解码成JsonArray类型，如果使用buffer以外的解码器则返回null
     */
    @Nullable
    JsonArray bodyAsJsonArray();

    /**
     * @return 返回响应主体，将其解码成JsonArray类型，然后转换成对象，如果使用buffer以外的解码器则返回null
     */
    @Nullable
    default <R> R bodyAsJson(Class<R> type) {
        Buffer b = bodyAsBuffer();
        return b != null ? BodyCodecImpl.jsonDecoder(type).apply(b) : null;
    }
}