package com.jokerliang.socket_netty_demo;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    SocketIOService socketIOService;

    @Autowired
    RabbitTemplate rabbitTemplate;

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
     * @param clientId
     * @return
     */
    @GetMapping("/test")
    public String test (String clientId) {
        String pushResult = "推送成功";
        Gson gson = new Gson();

        String orderCode = UUID.randomUUID().toString();
        ShipmentEntity shipmentEntity = new ShipmentEntity(orderCode, 1);

        String shipmentJson = gson.toJson(shipmentEntity);

        PushMessage pushMessage =
                new PushMessage(PushMessage.EVENT_SHIPMENT, shipmentJson);

        try {
            Boolean isPushSuccess = push(0, clientId, pushMessage);
            if (!isPushSuccess) {
                pushResult = "推送失败";
            }
        } catch (ClientOffLineException e) {
            pushResult = e.getMessage();
        }
        log.info(pushResult);

        return pushResult;
    }


    private Boolean push(int count, String clientId, PushMessage pushMessage) {

        UUID uuid = UUID.fromString(clientId);
        boolean isSuccess;
        SocketPushResult socketPushResult = socketIOService.pushMessage(uuid, pushMessage);
        isSuccess = socketPushResult.isSuccess();
        log.info(socketPushResult.getResult());
        if (isSuccess) {
            return true;
        }
        count += 1;
        if (count < 5) {
            push(count, clientId, pushMessage);
        }

        return false;
    }
}
