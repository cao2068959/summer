package com.chy.summer.link.common.codec;

import com.chy.summer.link.common.codec.impl.BodyCodecImpl;
import com.chy.summer.link.common.codec.impl.JsonStreamBodyCodec;
import com.chy.summer.link.common.codec.impl.StreamingBodyCodec;
import com.chy.summer.link.common.codec.spi.BodyStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.streams.WriteStream;

import java.util.function.Function;

/**
 * 用于对HTTP body进行编码和解码的工具接口
 *
 * @param <T>
 */
public interface BodyCodec<T> {

    /**
     * @return UTF-8字符串编解码器
     */
    static BodyCodec<String> string() {
        return BodyCodecImpl.STRING;
    }

    /**
     * 使用指定编码的字符串编解码器
     *
     * @param encoding 指定的编码
     * @return 编解码器
     */
    static BodyCodec<String> string(String encoding) {
        return BodyCodecImpl.string(encoding);
    }

    /**
     * @return {@link Buffer}编解码器
     */
    static BodyCodec<Buffer> buffer() {
        return BodyCodecImpl.BUFFER;
    }

    /**
     * @return {@link JsonObject}编解码器
     */
    static BodyCodec<JsonObject> jsonObject() {
        return BodyCodecImpl.JSON_OBJECT;
    }

    /**
     * @return {@link JsonArray}编解码器
     */
    static BodyCodec<JsonArray> jsonArray() {
        return BodyCodecImpl.JSON_ARRAY;
    }

    /**
     * 使用Jackson映射java对象的编解码器
     *
     * @return 用于将POJO映射到Json的编解码器
     */
    static <U> BodyCodec<U> json(Class<U> type) {
        return BodyCodecImpl.json(type);
    }

    /**
     * @return 返回一个丢弃响应的编解码器
     */
    static BodyCodec<Void> none() {
        return BodyCodecImpl.NONE;
    }

    /**
     * 创建一个可缓存整个body的编解码器，然后应用解码功能并返回结果
     *
     * @param decode 解码后的功能
     * @return 创建的编解码器
     */
    static <T> BodyCodec<T> create(Function<Buffer, T> decode) {
        return new BodyCodecImpl<>(decode);
    }

    /**
     * body编解码器，用于将body通过管道传递到写入流
     * 与方法pipe(stream, true)相同
     *
     * @param stream 目标的流
     * @return 写入流的body编解码器
     */
    static BodyCodec<Void> pipe(WriteStream<Buffer> stream) {
        return pipe(stream, true);
    }

    /**
     * body编解码器，用于将body通过管道传递到写入流
     *
     * @param stream 目标的流
     * @param close  是否关闭目标流
     * @return 写入流的body编解码器
     */
    static BodyCodec<Void> pipe(WriteStream<Buffer> stream, boolean close) {
        return new StreamingBodyCodec(stream, close);
    }

    /**
     * body编解码器，用于将body解析为JSON流
     *
     * @param parser 发送JSON对象的非空JSON解析器,必须为配置流解析器
     * @return 写流的body编解码器
     */
    static BodyCodec<Void> jsonStream(JsonParser parser) {
        return new JsonStreamBodyCodec(parser);
    }

    /**
     * 创建{@link BodyStream}.
     * 通常会调用此方法来为HTTP响应创建泵，但不应该直接调用此方法
     */
    void create(Handler<AsyncResult<BodyStream<T>>> handler);

}