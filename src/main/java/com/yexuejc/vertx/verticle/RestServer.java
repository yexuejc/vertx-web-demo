package com.yexuejc.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class RestServer extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        // 增加一个处理器，将请求的上下文信息，放到RoutingContext中
        router.route().handler(BodyHandler.create());
        // 增加cookies处理器，解码cookies，并将其放到context上下文中
        router.route().handler(CookieHandler.create());
        // 增加session处理器，为每次用户请求，维护一个唯一的session
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        //处理session
        router.route().handler(ctx -> {
            Session session = ctx.session();
            Integer count = session.get("count");
            if (count == null) {
                count = 0;
            }
            System.out.println("session:" + count);
            count++;
            session.put("count", count);
            ctx.vertx().setTimer(1, it -> ctx.next());
        });
        //处理cookie
        router.route().handler(routingContext -> {
            Cookie cookie = routingContext.getCookie("testCookie");
            Integer c = 0;
            if (cookie != null) {
                String count = cookie.getValue();
                try {
                    c = Integer.valueOf(count);
                } catch (Exception e) {
                    c = 0;
                }
                c++;
            }
            System.out.println("cookie:" + c);
            routingContext.addCookie(Cookie.cookie("testCookie", String.valueOf(c)));
            routingContext.vertx().setTimer(1, it -> routingContext.next());
        });


        router.route("/post/:param1/:param2").handler(this::handlePost);
        router.route("/get/:param1/:param2").handler(this::handleGet);
        router.route("/spring/hello").handler(this::springHello);
        router.route("/login").handler(BaseHandler.create(SpringVerticle.SERVICE_ADDRESS_LOGIN));


        vertx.createHttpServer().requestHandler(router::accept).listen(8081, it -> {
            if (it.succeeded()) {
                System.out.println("启动成功");
            } else {
                System.out.println("启动失败");
            }
        });
    }

    private void springHello(RoutingContext context) {
        vertx.eventBus().send(
                SpringVerticle.GET_HELLO_MSG_SERVICE_ADDRESS,// 消息地址
                "event bus calls spring service",// 消息内容
                result -> { // 异步结果处理
                    if (result.succeeded()) {
                        // 成功的话，返回处理结果给前台，这里的处理结果就是service返回的一段字符串
                        context.response()
                                .putHeader("content-type",
                                        "application/json")
                                .end(String.valueOf(result.result().body()));
                    } else {
                        context.response().setStatusCode(400)
                                .end(result.cause().toString());
                    }
                }
        );
    }

    private Handler<RoutingContext> hello() {
        return ctx -> {
            JsonObject object = ctx.getBodyAsJson();
            System.out.println("参数：" + object.toString());
            ctx.response().setStatusCode(200).putHeader("content-type", "text/plain").end("hello");
        };
    }


    /**
     * 处理Get
     *
     * @param context
     */
    private void handleGet(RoutingContext context) {
        String param1 = context.request().getParam("param1");
        String param2 = context.request().getParam("param2");
        if (isBlank(param1) || isBlank(param2)) {
            context.response().setStatusCode(400).end();
        }

        JsonObject obj = new JsonObject();
        obj.put("method", "get").put("param1", param1).put("param2", param2);
        context.response().putHeader("content-type", "application/json")
                .end(obj.encodePrettily());

    }

    /**
     * 处理post
     *
     * @param context
     */
    private void handlePost(RoutingContext context) {
        String param1 = context.request().getParam("param1");
        String param2 = context.request().getParam("param2");

        if (isBlank(param1) || isBlank(param2)) {
            context.response().setStatusCode(400).end();
        }
        JsonObject obj = new JsonObject();
        obj.put("method", "post").put("param1", param1).put("param2", param2);

        context.response().putHeader("content-type", "application/json")
                .end(obj.encodePrettily());
    }

    private boolean isBlank(String str) {
        if (str == null || "".equals(str))
            return true;
        return false;
    }
}
