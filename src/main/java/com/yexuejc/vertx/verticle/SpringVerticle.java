package com.yexuejc.vertx.verticle;

import com.yexuejc.vertx.service.SpringServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;

public class SpringVerticle extends AbstractVerticle {
    public static final String GET_HELLO_MSG_SERVICE_ADDRESS = "get_hello_msg_service";
    public static final String SERVICE_ADDRESS_LOGIN = "service_login";

    private SpringServer service;

    /**
     * service层注入
     *
     * @param context
     */
    public SpringVerticle(ApplicationContext context) {
        service = (SpringServer) context.getBean("springService");
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus()
                .consumer(GET_HELLO_MSG_SERVICE_ADDRESS)
                .handler(msg -> {
                    // 获取事件内容后，调用service服务
                    System.out.println("bus msg body is:" + msg.body());
                    String helloMsg = service.getHello();
                    System.out.println("msg from hello service is: "
                            + helloMsg);
                    // 将service返回的字符串，回应给消息返回体
                    msg.reply(helloMsg);
                });
        vertx.eventBus().consumer(SERVICE_ADDRESS_LOGIN).handler(it -> {
            if (it.body() == null) {
                it.reply("参数错误");
            } else {
                it.reply(service.Login((JsonObject) it.body()));
            }

        });

    }
}
