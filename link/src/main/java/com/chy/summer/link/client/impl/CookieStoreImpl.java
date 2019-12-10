package com.chy.summer.link.client.impl;

import com.chy.summer.link.client.spi.CookieStore;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.http.impl.HttpUtils;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

/**
 * Cookie储存管理的实现
 */
public class CookieStoreImpl implements CookieStore {
  /**
   * 没有域的cookie
   */
  private ConcurrentHashMap<Key, Cookie> noDomainCookies;
  /**
   * 带域的cookie
   */
  private ConcurrentSkipListMap<Key, Cookie> domainCookies;
  
  public CookieStoreImpl() {
    noDomainCookies = new ConcurrentHashMap<>();
    domainCookies = new ConcurrentSkipListMap<>();
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
  @Override
  public Iterable<Cookie> get(Boolean ssl, String domain, String path) {
    assert domain != null && domain.length() > 0;

    String cleanPath;
    {
      String uri = HttpUtils.removeDots(path);
      // 删除掉查询条件
      int pos = uri.indexOf('?');
      if (pos > -1) {
        uri = uri.substring(0, pos);
      }
  
      // 如果有分片标识符，就删除
      pos = uri.indexOf('#');
      if (pos > -1) {
        uri = uri.substring(0, pos);
      }
      cleanPath = uri;
    }

    //有序map
    TreeMap<String, Cookie> matches = new TreeMap<>();

    //处理cookie的函数
    Consumer<Cookie> adder = c -> {
      if ( !Boolean.TRUE.equals(ssl) && c.isSecure()) {
        //没使用ssl，并且是安全的
        return;
      }
      if (c.path() != null && !cleanPath.equals(c.path())) {
        String cookiePath = c.path();
        //结尾添加“/”
        if (!cookiePath.endsWith("/")) {
          cookiePath += '/';
        }
        if (!cleanPath.startsWith(cookiePath)) {
          return;
        }
      }
      //添加匹配
      matches.put(c.name(), c);      
    };

    //对每个没有域的cookie进行处理
    for (Cookie c : noDomainCookies.values()) {
      adder.accept(c);
    }
    
    Key key = new Key(domain, "", "");
    String prefix = key.domain.substring(0, 1);
    for (Map.Entry<Key, Cookie> entry : domainCookies.tailMap(new Key(prefix, "", ""), true).entrySet()) {
      if (entry.getKey().domain.compareTo(key.domain) > 0) {
        break;
      }
      if (!key.domain.startsWith(entry.getKey().domain)) {
        continue;
      }
      adder.accept(entry.getValue());
    }
        
    return matches.values();
  }

  /**
   * 添加一个cookie到这个{@code CookieStore}
   *
   * 如果从服务器收到了相同名称的cookie，会覆盖掉同名参数值
   *
   * @param cookie 需要加入的{@link Cookie}
   * @return 返回当前设置的对象，用于链式调用
   */
  @Override
  public CookieStore put(Cookie cookie) {
    Key key = new Key(cookie.domain(), cookie.path(), cookie.name());
    if (key.domain.equals(Key.NO_DOMAIN)) {
      noDomainCookies.put(key, cookie);
      return this;
    }
    domainCookies.put(key, cookie);
    return this;
  }

  /**
   * 删除之前添加的cookie
   *
   * @param cookie 需要删除的{@link Cookie}
   * @return 返回当前设置的对象，用于链式调用
   */
  @Override
  public CookieStore remove(Cookie cookie) {
    Key key = new Key(cookie.domain(), cookie.path(), cookie.name());
    if (key.domain.equals(Key.NO_DOMAIN)) {
      noDomainCookies.remove(key);
    } else {
      domainCookies.remove(key);
    }
    return this;
  }

  private static class Key implements Comparable<Key> {
    private static final String NO_DOMAIN = "";

    private final String domain;
    private final String path;
    private final String name;

    public Key(String domain, String path, String name) {
      if (domain == null || domain.length() == 0) {
        this.domain = NO_DOMAIN;
      } else {
        while (domain.charAt(0) == '.') {
          domain = domain.substring(1);
        }
        while (domain.charAt(domain.length() - 1) == '.') {
          domain = domain.substring(0, domain.length() - 1);
        }
        if (domain.length() == 0) {
          this.domain = NO_DOMAIN;
        } else {
          String[] tokens = domain.split("\\.");
          String tmp;
          for (int i = 0, j = tokens.length - 1; i < tokens.length / 2; ++i, --j) {
            tmp = tokens[j];
            tokens[j] = tokens[i];
            tokens[i] = tmp;
          }
          this.domain = String.join(".", tokens);
        }
      }
      this.path = path == null ? "" : path;
      this.name = name;
    }

    @Override
    public int compareTo(Key o) {
      int ret = domain.compareTo(o.domain);
      if (ret == 0) {
        ret = path.compareTo(o.path);
      }
      if (ret == 0) {
        ret = name.compareTo(o.name);
      }
      return ret;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((domain == null) ? 0 : domain.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Key other = (Key) obj;
      if (domain == null) {
        if (other.domain != null) {
          return false;
        }
      } else if (!domain.equals(other.domain)) {
        return false;
      }
      if (name == null) {
        if (other.name != null) {
          return false;
        }
      } else if (!name.equals(other.name)) {
        return false;
      }
      if (path == null) {
        return other.path == null;
      } else {
        return path.equals(other.path);
      }
    }
  }  
}