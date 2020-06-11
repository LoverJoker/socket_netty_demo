package com.jokerliang.socket_netty_demo;


import com.jokerliang.socket_netty_demo.socket.ServerMessageHandler;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:27
 */
@Slf4j
@RestController
public class TestController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/rabbit")
    public String rabbitTest() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "message: testFanoutMessage ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);
//        rabbitTemplate.convertAndSend("fanoutExchange", null, map);

        stringRedisTemplate.convertAndSend("channel:test", "test");

        return "true";
    }


    /**
     * 当有一条消息需要发送到客户端的时候，应该首先发送给rabbitMq, 然后由rabbitMq分发到相应的服务端
     * @return
     */
    @GetMapping("/test")
    public String test (String cmd) {
        ReceiveLog receiveLog = new ReceiveLog();
        Date date = new Date();
        receiveLog.setDate(date);
        receiveLog.setMsg("测试11");
        mongoTemplate.insert(receiveLog);
        ServerMessageHandler.sendMessage(ServerMessageHandler.DEVICE_CODE_FILED_NAME, cmd);
        return "success";
    }




}
