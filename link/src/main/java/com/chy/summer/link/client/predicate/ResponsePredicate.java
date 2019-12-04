package com.chy.summer.link.client.predicate;

import com.chy.summer.link.client.HttpResponse;
import com.chy.summer.link.client.impl.predicate.ResponsePredicateImpl;
import io.vertx.core.http.HttpHeaders;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * HttpResponse的断言接口
 */
public interface ResponsePredicate extends Function<HttpResponse<Void>, ResponsePredicateResult> {

  /**
   * 任何1XX的信息响应
   */
  ResponsePredicate SC_INFORMATIONAL_RESPONSE = status(100, 200);

  /**
   * 100 继续
   */
  ResponsePredicate SC_CONTINUE = status(100);

  /**
   * 101 交换协议
   */
  ResponsePredicate SC_SWITCHING_PROTOCOLS = status(101);

  /**
   * 102 由WebDAV（RFC2518）扩展的状态码，代表处理将被继续执行
   */
  ResponsePredicate SC_PROCESSING = status(102);

  /**
   * 103 浏览器预加载这些外部资源链接，服务器在发送主头文件前先发送包含外部资源链接的头文件，
   * 浏览器客户端因此能在主头文件到来前预加载 CSS 和 JavaScript 文件。
   */
  ResponsePredicate SC_EARLY_HINTS = status(103);

  /**
   * 任何2XX的信息响应
   */
  ResponsePredicate SC_SUCCESS = status(200, 300);

  /**
   * 200 请求成功
   */
  ResponsePredicate SC_OK = status(200);

  /**
   * 201 表示请求已经被成功处理,并且创建了新的资源
   */
  ResponsePredicate SC_CREATED = status(201);

  /**
   * 202 表示服务器端已经收到请求消息,但是尚未进行处理
   */
  ResponsePredicate SC_ACCEPTED = status(202);

  /**
   * 203 表明请求已成功,但封装的有效负载已由源服务器的200(OK)响应由变换代理修改
   */
  ResponsePredicate SC_NON_AUTHORITATIVE_INFORMATION = status(203);

  /**
   * 204 表示目前请求成功,但客户端不需要更新其现有页面
   */
  ResponsePredicate SC_NO_CONTENT = status(204);

  /**
   * 205 用来通知客户端重置文档视图，比如清空表单内容、重置 canvas 状态或者刷新用户界面
   */
  ResponsePredicate SC_RESET_CONTENT = status(205);

  /**
   * 206 表示请求已成功，并且主体包含所请求的数据区间，该数据区间是在请求的 Range 首部指定的
   */
  ResponsePredicate SC_PARTIAL_CONTENT = status(206);

  /**
   * 207 由WebDAV(RFC 2518)扩展的状态码，代表之后的消息体将是一个XML消息，并且可能依照之前子请求数量的不同，包含一系列独立的响应代码
   */
  ResponsePredicate SC_MULTI_STATUS = status(207);

  /**
   * 任何3XX的信息响应
   */
  ResponsePredicate SC_REDIRECTION = status(300, 400);

  /**
   * 300 表示重定向的响应状态码，表示该请求拥有多种可能的响应。
   * 用户代理或者用户自身应该从中选择一个。由于没有如何进行选择的标准方法，这个状态码极少使用
   */
  ResponsePredicate SC_MULTIPLE_CHOICES = status(300);

  /**
   * 301 永久重定向 说明请求的资源已经被移动到了由 Location 头部指定的url上，是固定的不会再改变。搜索引擎会根据该响应修正
   */
  ResponsePredicate SC_MOVED_PERMANENTLY = status(301);

  /**
   * 302 重定向状态码表明请求的资源被暂时的移动到了由Location 头部指定的 URL 上。
   * 浏览器会重定向到这个URL， 但是搜索引擎不会对该资源的链接进行更新
   */
  ResponsePredicate SC_FOUND = status(302);

  /**
   * 303 重定向状态码，通常作为 PUT 或 POST 操作的返回结果，它表示重定向链接指向的不是新上传的资源，而是另外一个页面，比如消息确认页面或上传进度页面
   */
  ResponsePredicate SC_SEE_OTHER = status(303);

  /**
   * 304 说明无需再次传输请求的内容，也就是说可以使用缓存的内容。这通常是在一些安全的方法
   */
  ResponsePredicate SC_NOT_MODIFIED = status(304);

  /**
   * 305 被请求的资源必须通过指定的代理才能被访问
   */
  ResponsePredicate SC_USE_PROXY = status(305);

  /**
   * 307 请求的资源现在临时从不同的URI 响应请求。由于这样的重定向是临时的，客户端应当继续向原有地址发送以后的请求。
   */
  ResponsePredicate SC_TEMPORARY_REDIRECT = status(307);

  /**
   * 308 这意味着资源现在永久位于由 Location: HTTP Response 标头指定的另一个 URI
   */
  ResponsePredicate SC_PERMANENT_REDIRECT = status(308);

  /**
   * 任何4XX的信息响应
   */
  ResponsePredicate SC_CLIENT_ERRORS = status(400, 500);

  /**
   * 400 语义有误，当前请求无法被服务器理解。除非进行修改，否则客户端不应该重复提交这个请求
   * 请求参数有误
   */
  ResponsePredicate SC_BAD_REQUEST = status(400);

  /**
   * 401 当前请求需要用户验证
   */
  ResponsePredicate SC_UNAUTHORIZED = status(401);

  /**
   * 402 此响应码保留以便将来使用，创造此响应码的最初目的是用于数字支付系统，然而现在并未使用
   */
  ResponsePredicate SC_PAYMENT_REQUIRED = status(402);

  /**
   * 403 服务器已经理解请求，但是拒绝执行它
   */
  ResponsePredicate SC_FORBIDDEN = status(403);

  /**
   * 404 请求失败，请求所希望得到的资源未被在服务器上发现
   */
  ResponsePredicate SC_NOT_FOUND = status(404);

  /**
   * 405 请求行中指定的请求方法不能被用于请求相应的资源
   */
  ResponsePredicate SC_METHOD_NOT_ALLOWED = status(405);

  /**
   * 406 请求的资源的内容特性无法满足请求头中的条件，因而无法生成响应实体
   */
  ResponsePredicate SC_NOT_ACCEPTABLE = status(406);

  /**
   * 407 与401响应类似，只不过客户端必须在代理服务器上进行身份验证
   */
  ResponsePredicate SC_PROXY_AUTHENTICATION_REQUIRED = status(407);

  /**
   * 408 请求超时。客户端没有在服务器预备等待的时间内完成一个请求的发送
   */
  ResponsePredicate SC_REQUEST_TIMEOUT = status(408);

  /**
   * 409 由于和被请求的资源的当前状态之间存在冲突，请求无法完成
   */
  ResponsePredicate SC_CONFLICT = status(409);

  /**
   * 410 被请求的资源在服务器上已经不再可用，而且没有任何已知的转发地址
   */
  ResponsePredicate SC_GONE = status(410);

  /**
   * 411 服务器拒绝在没有定义 Content-Length 头的情况下接受请求
   */
  ResponsePredicate SC_LENGTH_REQUIRED = status(411);

  /**
   * 412 服务器在验证在请求的头字段中给出先决条件时，没能满足其中的一个或多个
   */
  ResponsePredicate SC_PRECONDITION_FAILED = status(412);

  /**
   * 413 服务器拒绝处理当前请求，因为该请求提交的实体数据大小超过了服务器愿意或者能够处理的范围
   */
  ResponsePredicate SC_REQUEST_ENTITY_TOO_LARGE = status(413);

  /**
   * 414 请求的URI 长度超过了服务器能够解释的长度，因此服务器拒绝对该请求提供服务
   */
  ResponsePredicate SC_REQUEST_URI_TOO_LONG = status(414);

  /**
   * 415 对于当前请求的方法和所请求的资源，请求中提交的实体并不是服务器中所支持的格式，因此请求被拒绝。
   */
  ResponsePredicate SC_UNSUPPORTED_MEDIA_TYPE = status(415);

  /**
   * 416 如果请求中包含了 Range 请求头，并且 Range 中指定的任何数据范围都与当前资源的可用范围不重合，
   * 同时请求中又没有定义 If-Range 请求头，那么服务器就应当返回416状态码
   */
  ResponsePredicate SC_REQUESTED_RANGE_NOT_SATISFIABLE = status(416);

  /**
   * 417 此响应代码意味着服务器无法满足 Expect 请求标头字段指示的期望值
   */
  ResponsePredicate SC_EXPECTATION_FAILED = status(417);

  /**
   * 421 该请求针对的是无法产生响应的服务器。 这可以由服务器发送，该服务器未配置为针对包含在请求 URI 中的方案和权限的组合产生响应
   */
  ResponsePredicate SC_MISDIRECTED_REQUEST = status(421);

  /**
   * 422 请求格式良好，但由于语义错误而无法遵循
   */
  ResponsePredicate SC_UNPROCESSABLE_ENTITY = status(422);

  /**
   * 423 正在访问的资源被锁定
   */
  ResponsePredicate SC_LOCKED = status(423);

  /**
   * 424 由于先前的请求失败，所以此次请求失败
   */
  ResponsePredicate SC_FAILED_DEPENDENCY = status(424);

  /**
   * 425 服务器不愿意冒着风险去处理可能重播的请求
   */
  ResponsePredicate SC_UNORDERED_COLLECTION = status(425);

  /**
   * 426 服务器拒绝使用当前协议执行请求，但可能在客户机升级到其他协议后愿意这样做
   */
  ResponsePredicate SC_UPGRADE_REQUIRED = status(426);

  /**
   * 428 原始服务器要求该请求是有条件的
   */
  ResponsePredicate SC_PRECONDITION_REQUIRED = status(428);

  /**
   * 429 用户在给定的时间内发送了太多请求（“限制请求速率”）
   */
  ResponsePredicate SC_TOO_MANY_REQUESTS = status(429);

  /**
   * 431 服务器不愿意处理请求，因为它的 请求头字段太大
   */
  ResponsePredicate SC_REQUEST_HEADER_FIELDS_TOO_LARGE = status(431);

  /**
   * 任何5XX的信息响应
   */
  ResponsePredicate SC_SERVER_ERRORS = status(500, 600);

  /**
   * 500 服务器遇到了不知道如何处理的情况
   */
  ResponsePredicate SC_INTERNAL_SERVER_ERROR = status(500);

  /**
   * 501 此请求方法不被服务器支持且无法被处理。只有GET和HEAD是要求服务器支持的，它们必定不会返回此错误代码
   */
  ResponsePredicate SC_NOT_IMPLEMENTED = status(501);

  /**
   * 502 此错误响应表明服务器作为网关需要得到一个处理这个请求的响应，但是得到一个错误的响应
   */
  ResponsePredicate SC_BAD_GATEWAY = status(502);

  /**
   * 503 服务器没有准备好处理请求。 常见原因是服务器因维护或重载而停机
   */
  ResponsePredicate SC_SERVICE_UNAVAILABLE = status(503);

  /**
   * 504 当服务器作为网关，不能及时得到响应时返回此错误代码
   */
  ResponsePredicate SC_GATEWAY_TIMEOUT = status(504);

  /**
   * 505 服务器不支持请求中所使用的HTTP协议版本
   */
  ResponsePredicate SC_HTTP_VERSION_NOT_SUPPORTED = status(505);

  /**
   * 506 服务器有一个内部配置错误：对请求的透明内容协商导致循环引用
   */
  ResponsePredicate SC_VARIANT_ALSO_NEGOTIATES = status(506);

  /**
   * 507 服务器有内部配置错误：所选的变体资源被配置为参与透明内容协商本身，因此不是协商过程中的适当端点
   */
  ResponsePredicate SC_INSUFFICIENT_STORAGE = status(507);

  /**
   * 510 客户端需要对请求进一步扩展，服务器才能实现它
   */
  ResponsePredicate SC_NOT_EXTENDED = status(510);

  /**
   * 511 状态码指示客户端需要进行身份验证才能获得网络访问权限
   */
  ResponsePredicate SC_NETWORK_AUTHENTICATION_REQUIRED = status(511);

  /**
   * 创建一个断言，断言状态响应代码等于{@code statusCode}.
   *
   * @param statusCode 预期状的态码
   */
  static ResponsePredicate status(int statusCode) {
    return status(statusCode, statusCode + 1);
  }

  /**
   * 创建一个断言，断言状态响应代码在[min，max]范围内
   *
   * @param min 较低的接受的状态码（包括当前值）
   * @param max 较高的接受的状态码（不包括当前值）
   */
  static ResponsePredicate status(int min, int max) {
    return response -> {
      //获取当前的响应状态值
      int sc = response.statusCode();
      //判断是否在范围内
      if (sc >= min && sc < max) {
        return ResponsePredicateResult.success();
      }
      if (max - min == 1) {
        return ResponsePredicateResult.failure("响应状态码" + sc + "不等于" + min);
      }
      return ResponsePredicateResult.failure("响应状态码" + sc + "不在" + min + "和" + max+"之间");
    };
  }

  /**
   * 创建一个断言来验证响应的{@code content-type}是否为{@code application/json}
   */
  ResponsePredicate JSON = contentType("application/json");

  /**
   * 创建一个断言来验证响应的{@code content-type}是否为 {@code mimeType}.
   *
   * @param mimeType mime类型
   */
  static ResponsePredicate contentType(String mimeType) {
    return ResponsePredicate.contentType(Collections.singletonList(mimeType));
  }

  /**
   * 创建一个断言来验证响应的{@code content-type}匹配{@code mimeTypes}中的一个.
   *
   * @param mimeTypes mime类型的集合
   */
  static ResponsePredicate contentType(List<String> mimeTypes) {
    return response -> {
      //获取请求的contentType
      String contentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
      if (contentType == null) {
        return ResponsePredicateResult.failure("缺少响应内容类型");
      }
      for (String mimeType : mimeTypes) {
        //比较当前的contentType是否在列表中
        if (contentType.equalsIgnoreCase(mimeType)) {
          return ResponsePredicateResult.success();
        }
      }
      StringBuilder sb = new StringBuilder("希望的contentType ").append(contentType).append("以下内容之一");
      boolean first = true;
      for (String mimeType : mimeTypes) {
        if (!first) {
          sb.append(", ");
        }
        first = false;
        sb.append(mimeType);
      }
      return ResponsePredicateResult.failure(sb.toString());
    };
  }

  /**
   * 创建一个新的ResponsePredicate。
   *
   * @param test 收到响应时要调用的函数
   */
  static ResponsePredicate create(Function<HttpResponse<Void>, ResponsePredicateResult> test) {
    return test::apply;
  }

  /**
   * 使用自定义的errorConverter创建一个新的ResponsePredicate
   *
   * @param test 收到响应时要调用的函数
   * @param errorConverter 将test函数的结果转换为Throwable
   */
  static ResponsePredicate create(Function<HttpResponse<Void>, ResponsePredicateResult> test, ErrorConverter errorConverter) {
    return new ResponsePredicateImpl(test, errorConverter);
  }

  /**
   * @return 当前使用的错误转换器
   */
  default ErrorConverter errorConverter() {
    return ErrorConverter.DEFAULT_CONVERTER;
  }
}