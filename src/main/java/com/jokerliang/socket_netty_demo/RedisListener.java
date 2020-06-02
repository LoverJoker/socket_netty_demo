package com.jokerliang.socket_netty_demo;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-02 12:17
 */
@Slf4j
@Component
public class RedisListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.info("收到消息了:: " + message);
    }
}
