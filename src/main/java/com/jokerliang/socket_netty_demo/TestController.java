package com.jokerliang.socket_netty_demo;



import com.jokerliang.socket_netty_demo.device.ByteUtils;
import com.jokerliang.socket_netty_demo.device.GarshponMachine;
import com.jokerliang.socket_netty_demo.socket.ServerMessageHandler;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public String test (String cmd, String deviceCode) {
        ServerMessageHandler.sendMessage(deviceCode, cmd);
        return "success";
    }

    @GetMapping("/update")
    public String update(String deviceCode) throws IOException {
        log.info("----------------------注意开始此次更新----------------------");
        byte[] downFrame = GarshponMachine.Update.getDownFrame(1);
        ServerMessageHandler.sendMessage(deviceCode, downFrame);
        return "success";
    }

    @GetMapping("/pay")
    public String pay(String deviceCode) {
        byte[] orderCode = GarshponMachine.Pay.getOrderCode();
        log.info("------申请支付开始:" +  ByteUtils.byteArrayToHexString(orderCode));
        byte[] bytes = GarshponMachine.Pay.applyPay((byte) 0x01, orderCode);
        ServerMessageHandler.sendMessage(deviceCode, bytes);
        return "发送的数据：" + ByteUtils.byteArrayToHexString(bytes);
    }

    @GetMapping("/volume")
    public String volume(String deviceCode, int num) {
        byte[] bytes = GarshponMachine.Params.setVolume(num);
        ServerMessageHandler.sendMessage(deviceCode, bytes);
        return  "发送的数据：" + ByteUtils.byteArrayToHexString(bytes);
    }

    @GetMapping("/getVolume")
    public String getVolume(String deviceCode) {
        byte[] bytes = GarshponMachine.Params.queryVolume();
        ServerMessageHandler.sendMessage(deviceCode, bytes);
        return  "发送的数据：" + ByteUtils.byteArrayToHexString(bytes);
    }

}
