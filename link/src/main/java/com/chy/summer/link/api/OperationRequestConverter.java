package com.chy.summer.link.api;

import io.vertx.core.json.JsonObject;

/**
 * OperationRequest转换类
 */
public class OperationRequestConverter {


   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OperationRequest obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "extra":
          if (member.getValue() instanceof JsonObject) {
            obj.setExtra(((JsonObject)member.getValue()).copy());
          }
          break;
        case "params":
          if (member.getValue() instanceof JsonObject) {
            obj.setParams(((JsonObject)member.getValue()).copy());
          }
          break;
        case "user":
          if (member.getValue() instanceof JsonObject) {
            obj.setUser(((JsonObject)member.getValue()).copy());
          }
          break;
      }
    }
  }

   static void toJson(OperationRequest obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(OperationRequest obj, java.util.Map<String, Object> json) {
    if (obj.getExtra() != null) {
      json.put("extra", obj.getExtra());
    }
    if (obj.getParams() != null) {
      json.put("params", obj.getParams());
    }
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
  }
}