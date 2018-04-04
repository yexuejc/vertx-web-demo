package com.yexuejc.vertx.verticle;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public interface BaseHandler extends Handler<RoutingContext> {
    static BaseHandler create(String serviceAddress) {
        return new BaseHandlerImpl(serviceAddress);
    }
}

class BaseHandlerImpl implements BaseHandler {
    /**
     * 消息地址
     */
    private String serviceAddress;

    public BaseHandlerImpl(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    @Override
    public void handle(RoutingContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("username", context.request().getParam("username"))
                .put("password", context.request().getParam("password"));
        context.vertx().eventBus().send(
                serviceAddress,// 消息地址
                jsonObject,// 消息内容:参数传递
                result -> { // 异步结果处理
                    if (result.succeeded()) {
                        // 成功的话，返回处理结果给前台，这里的处理结果就是service返回的一段字符串
                        JsonObject object = (JsonObject) result.result().body();
                        if (200 == object.getInteger("code")) {
                            System.out.println(object.getString("data"));
                            context.session().put("loginUserToken", object.getString("data"));
                        }
                        context.response()
                                .putHeader("content-type",
                                        "application/json")
                                .end(String.valueOf(result.result().body()));
                    } else {
                        //失败处理
                        context.response().setStatusCode(400)
                                .end(result.cause().toString());
                    }
                }
        );
    }
}
