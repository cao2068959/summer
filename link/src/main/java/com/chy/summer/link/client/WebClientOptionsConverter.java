package com.chy.summer.link.client;

import io.vertx.core.json.JsonObject;

/**
 * WebClientOptions的转换器和映射器
 */
public class WebClientOptionsConverter {

    /**
     * json转WebClientOptions
     *
     * @param json json数据
     * @param obj  目标对象
     */
    public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, WebClientOptions obj) {
        for (java.util.Map.Entry<String, Object> member : json) {
            switch (member.getKey()) {
                case "followRedirects":
                    if (member.getValue() instanceof Boolean) {
                        //设置是否重定向
                        obj.setFollowRedirects((Boolean) member.getValue());
                    }
                    break;
                case "userAgent":
                    if (member.getValue() instanceof String) {
                        //设置userAgent
                        obj.setUserAgent((String) member.getValue());
                    }
                    break;
                case "userAgentEnabled":
                    if (member.getValue() instanceof Boolean) {
                        //设置是否发送userAgent
                        obj.setUserAgentEnabled((Boolean) member.getValue());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * WebClientOptions转json
     *
     * @param obj  元数据
     * @param json json数据
     */
    public static void toJson(WebClientOptions obj, JsonObject json) {
        toJson(obj, json.getMap());
    }

    /**
     * WebClientOptions转json
     *
     * @param obj  元数据
     * @param json json数据
     */
    public static void toJson(WebClientOptions obj, java.util.Map<String, Object> json) {
        json.put("followRedirects", obj.isFollowRedirects());
        if (obj.getUserAgent() != null) {
            json.put("userAgent", obj.getUserAgent());
        }
        json.put("userAgentEnabled", obj.isUserAgentEnabled());
    }
}