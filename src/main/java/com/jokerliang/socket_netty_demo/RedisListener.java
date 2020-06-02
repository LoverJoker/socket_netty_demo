package com.jokerliang.socket_netty_demo;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-02 12:17
 */
@Slf4j
@Component
public class RedisListener implements MessageListener {

    @Autowired
    SocketIOService socketIOService;
    @Override
    public void onMessage(Message message, byte[] bytes) {
        String pushResult = "推送成功";
        String messageJson = message.toString();
        Gson gson = new Gson();
        PushMessage pushMessage = gson.fromJson(messageJson, PushMessage.class);
        try {
            Boolean isPushSuccess = push(0, pushMessage);
            if (!isPushSuccess) {
                pushResult = "推送失败";
            }
        } catch (ClientOffLineException e) {
            pushResult = e.getMessage();
        }
        log.info(pushResult);
    }


    private Boolean push(int count, PushMessage pushMessage) {
        boolean isSuccess;
        SocketPushResult socketPushResult = socketIOService.pushMessage(pushMessage);
        isSuccess = socketPushResult.isSuccess();
        log.info(socketPushResult.getResult());
        if (isSuccess) {
            return true;
        }
        count += 1;
        if (count < 5) {
            push(count, pushMessage);
        }

        return false;
    }

}
