package com.jokerliang.socket_netty_demo;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-02 11:00
 */
@Component
@RabbitListener(queues = "socketQueue")
public class TopicSocketRabbitMqConsumer2 {

    @RabbitHandler
    public void process(Map testMessage) {
        System.out.println("消费者收到消息  : " +testMessage.toString());
    }

}
