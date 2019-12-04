package com.chy.summer.link.common.codec.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * 通过WriteStream<Buffer>将body流解码为T实例
 * @param <T>
 */
public interface BodyStream<T> extends WriteStream<Buffer>, Handler<Throwable> {

  /**
   * @return 流处理完毕之后，回调方法
   */
  Future<T> result();

}