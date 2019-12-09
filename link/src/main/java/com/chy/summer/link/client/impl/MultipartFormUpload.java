package com.chy.summer.link.client.impl;

import com.chy.summer.link.common.multipart.FormDataPart;
import com.chy.summer.link.common.multipart.MultipartForm;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;

import java.io.File;

/**
 * 用于发送一个切片的表单流
 */
public class MultipartFormUpload implements ReadStream<Buffer> {

  private static final UnpooledByteBufAllocator ALLOC = new UnpooledByteBufAllocator(false);
  /**
   * 默认完整的http请求
   */
  private DefaultFullHttpRequest request;
  /**
   * HttpPost请求解码器
   */
  private HttpPostRequestEncoder encoder;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;
  private InboundBuffer<Buffer> pending;
  private boolean ended;
  /**
   * 上下文
   */
  private final Context context;

  public MultipartFormUpload(Context context,
                             MultipartForm parts,
                             boolean multipart,
                             HttpPostRequestEncoder.EncoderMode encoderMode) throws Exception {
    this.context = context;
    this.pending = new InboundBuffer<Buffer>(context).emptyHandler(v -> checkEnd()).drainHandler(v -> run()).pause();
    //一个简单的post请求
    this.request = new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1,
      io.netty.handler.codec.http.HttpMethod.POST,
      "/");
    this.encoder = new HttpPostRequestEncoder(
      new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE),
      request,
      multipart,
      //默认字符集
      HttpConstants.DEFAULT_CHARSET,
      encoderMode);
    for (FormDataPart formDataPart : parts) {
      if (formDataPart.isAttribute()) {
        //添加一个body的元素
        encoder.addBodyAttribute(formDataPart.name(), formDataPart.value());
      } else {
        //添加一个上传的文件
        encoder.addBodyFileUpload(formDataPart.name(),
          formDataPart.filename(), new File(formDataPart.pathname()),
          formDataPart.mediaType(), formDataPart.isText());
      }
    }
    //完成创建
    encoder.finalizeRequest();
  }

  private void checkEnd() {
    Handler<Void> handler;
    synchronized (MultipartFormUpload.this) {
      handler = ended ? endHandler : null;
    }
    if (handler != null) {
      handler.handle(null);
    }
  }

  public void run() {
    if (Vertx.currentContext() != context) {
      context.runOnContext(v -> {
        run();
      });
      return;
    }
    while (!ended) {
      if (encoder.isChunked()) {
        try {
          HttpContent chunk = encoder.readChunk(ALLOC);
          ByteBuf content = chunk.content();
          Buffer buff = Buffer.buffer(content);
          boolean writable = pending.write(buff);
          if (encoder.isEndOfInput()) {
            ended = true;
            request = null;
            encoder = null;
            if (pending.isEmpty()) {
              endHandler.handle(null);
            }
          } else if (!writable) {
            break;
          }
        } catch (Exception e) {
          ended = true;
          request = null;
          encoder = null;
          if (exceptionHandler != null) {
            exceptionHandler.handle(e);
          }
          break;
        }
      } else {
        ByteBuf content = request.content();
        Buffer buffer = Buffer.buffer(content);
        request = null;
        encoder = null;
        pending.write(buffer);
        ended = true;
        if (pending.isEmpty() && endHandler != null) {
          endHandler.handle(null);
        }
      }
    }
  }

  public MultiMap headers() {
    return new HeadersAdaptor(request.headers());
  }

  @Override
  public synchronized MultipartFormUpload exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public synchronized MultipartFormUpload handler(Handler<Buffer> handler) {
    pending.handler(handler);
    return this;
  }

  @Override
  public synchronized MultipartFormUpload pause() {
    pending.pause();
    return this;
  }

  @Override
  public ReadStream<Buffer> fetch(long amount) {
    pending.fetch(amount);
    return this;
  }

  @Override
  public synchronized MultipartFormUpload resume() {
    pending.resume();
    return this;
  }

  @Override
  public synchronized MultipartFormUpload endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }
}