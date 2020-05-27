package com.jokerliang.socket_netty_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:27
 */
@RestController
public class TestController {

    @Autowired
    SocketIOService socketIOService;

    @GetMapping("/test")
    public String test () {
        PushMessage pushMessage = new PushMessage();
        pushMessage.setContent("这是服务端来的消息");
        socketIOService.pushMessageToUser(pushMessage);
        return "success";
    }
}
