package com.yexuejc.vertx.service;

import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Component;

/**
 * spring 的 service
 */
@Component(value = "springService")
public class SpringServer {

    public String getHello() {
        return "hello spring vert.x";
    }

    /**
     * 登录
     *
     * @param object
     * @return
     */
    public JsonObject Login(JsonObject object) {
        System.out.println(object);
        JsonObject map = new JsonObject();
        if (object == null) {
            map.put("code", 400);
            map.put("msg", "参数错误");
            return map;
        }
        if (object.getString("username").isEmpty() || object.getString("password").isEmpty()
                || (!object.getString("username").equals("zhangsan") && !object.getString("username").equals("123456"))) {
            map.put("code", 400);
            map.put("msg", "用户名或密码错误");
            return map;
        }
        map.put("code", 200);
        map.put("msg", "登录成功");
        map.put("data", Math.random() + "");
        return map;
    }
}
