package com.chy.summer.link.common.codec.impl;

import com.chy.summer.link.common.codec.BodyCodec;
import com.chy.summer.link.common.codec.spi.BodyStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;

import java.util.function.Function;

/**
 * 用于对HTTP body进行编码和解码的工具实现
 *
 * @param <T>
 */
public class BodyCodecImpl<T> implements BodyCodec<T> {

    /**
     * 无效解码器
     */
    public static final Function<Buffer, Void> VOID_DECODER = buff -> null;
    /**
     * UTF-8解码器
     */
    public static final Function<Buffer, String> UTF8_DECODER = Buffer::toString;
    /**
     * Json解码器
     */
    public static final Function<Buffer, JsonObject> JSON_OBJECT_DECODER = buff -> {
        //将buff转成JsonObject
        Object val = Json.decodeValue(buff);
        if (val == null) {
            return null;
        }
        if (val instanceof JsonObject) {
            return (JsonObject) val;
        }
        throw new DecodeException("无效的Json Object解码为" + val.getClass().getName());
    };
    /**
     * Json数组解码器
     */
    public static final Function<Buffer, JsonArray> JSON_ARRAY_DECODER = buff -> {
        //将buff转成JsonArray
        Object val = Json.decodeValue(buff);
        if (val == null) {
            return null;
        }
        if (val instanceof JsonArray) {
            return (JsonArray) val;
        }
        throw new DecodeException("无效的Json Object解码为" + val.getClass().getName());
    };

    /**
     * 默认的UTF-8解码器
     */
    public static final BodyCodec<String> STRING = new BodyCodecImpl<>(UTF8_DECODER);
    /**
     * 默认的无效解码器
     */
    public static final BodyCodec<Void> NONE = new BodyCodecImpl<>(VOID_DECODER);
    /**
     * 默认的BUFFER解码器
     */
    public static final BodyCodec<Buffer> BUFFER = new BodyCodecImpl<>(Function.identity());
    /**
     * 默认的Json解码器
     */
    public static final BodyCodec<JsonObject> JSON_OBJECT = new BodyCodecImpl<>(JSON_OBJECT_DECODER);
    /**
     * 默认的Json数组解码器
     */
    public static final BodyCodec<JsonArray> JSON_ARRAY = new BodyCodecImpl<>(JSON_ARRAY_DECODER);

    public static BodyCodecImpl<String> string(String encoding) {
        return new BodyCodecImpl<>(buff -> buff.toString(encoding));
    }

    public static <T> BodyCodec<T> json(Class<T> type) {
        return new BodyCodecImpl<>(jsonDecoder(type));
    }

    public static <T> Function<Buffer, T> jsonDecoder(Class<T> type) {
        return buff -> Json.decodeValue(buff, type);
    }

    private final Function<Buffer, T> decoder;

    public BodyCodecImpl(Function<Buffer, T> decoder) {
        this.decoder = decoder;
    }

    /**
     * 创建{@link BodyStream}.
     * 通常会调用此方法来为HTTP响应创建泵，但不应该直接调用此方法
     */
    @Override
    public void create(Handler<AsyncResult<BodyStream<T>>> handler) {
        handler.handle(Future.succeededFuture(new BodyStream<T>() {

            Buffer buffer = Buffer.buffer();
            Promise<T> state = Promise.promise();

            @Override
            public void handle(Throwable cause) {
                state.tryFail(cause);
            }

            @Override
            public Future<T> result() {
                return state.future();
            }

            @Override
            public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
                return this;
            }

            @Override
            public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
                buffer.appendBuffer(data);
                handler.handle(Future.succeededFuture());
            }

            @Override
            public Future<Void> write(Buffer data) {
                buffer.appendBuffer(data);
                return Future.succeededFuture();
            }

            @Override
            public void end(Handler<AsyncResult<Void>> handler) {
                if (!state.future().isComplete()) {
                    T result;
                    if (buffer.length() > 0) {
                        try {
                            result = decoder.apply(buffer);
                        } catch (Throwable t) {
                            state.fail(t);
                            if (handler != null) {
                                handler.handle(Future.failedFuture(t));
                            }
                            return;
                        }
                    } else {
                        result = null;
                    }
                    state.complete(result);
                    if (handler != null) {
                        handler.handle(Future.succeededFuture());
                    }
                }
            }

            @Override
            public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
                return this;
            }

            @Override
            public boolean writeQueueFull() {
                return false;
            }

            @Override
            public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
                return this;
            }
        }));
    }
}