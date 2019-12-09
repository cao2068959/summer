package com.chy.summer.link.client.spi;

import com.chy.summer.link.client.impl.CookieStoreImpl;
import io.netty.handler.codec.http.cookie.Cookie;

/**
 * 为每个用户管理cookie储存
 */
public interface CookieStore {

  /**
   * 建立一个cookie存储
   * @return 一个新的cookie存储
   */
  static CookieStore build() {
    return new CookieStoreImpl();
  }
  
  /**
   * 返回一个cookie的迭代器，用于作为参数传递的过滤器
   * 实现在cookie存储中选一个适当的cookie返回并清理path
   *
   * @param ssl 如果使用ssl，则返回true
   * @param domain 正在使用的域
   * @param path 正在使用的path
   * @return 匹配到的cookie
   */
  Iterable<Cookie> get(Boolean ssl, String domain, String path);
  
  /**
   * 添加一个cookie到这个{@code CookieStore}
   *
   * 如果从服务器收到了相同名称的cookie，会覆盖掉同名参数值
   * 
   * @param cookie 需要加入的{@link Cookie}
   * @return 返回当前设置的对象，用于链式调用
   */
  CookieStore put(Cookie cookie);
  /**
   * 删除之前添加的cookie
   * 
   * @param cookie 需要删除的{@link Cookie}
   * @return 返回当前设置的对象，用于链式调用
   */
  CookieStore remove(Cookie cookie);
}