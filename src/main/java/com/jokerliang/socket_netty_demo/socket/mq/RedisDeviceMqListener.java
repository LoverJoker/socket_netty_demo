package com.jokerliang.socket_netty_demo.socket.mq;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-10 14:53
 */


import com.jokerliang.socket_netty_demo.GashaponPushMessage;
import com.jokerliang.socket_netty_demo.socket.GarshponServerMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-02 13:40
 */
@Slf4j
@Component
public class RedisDeviceMqListener implements MessageListener {



    @Override
    public void onMessage(Message message, byte[] bytes) {
        String messageJson = message.toString();
        log.info("RedisDeviceMqListener:onMessage" + messageJson);
        GashaponPushMessage gashaponPushMessage = GashaponPushMessage.fromJson(messageJson);
        GarshponServerMessageHandler.sendMessage(gashaponPushMessage.getDeviceCode(), gashaponPushMessage.getMessage());
    }


}

