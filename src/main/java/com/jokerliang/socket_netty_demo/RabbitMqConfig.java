package com.jokerliang.socket_netty_demo;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-02 10:52
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue socketQueue() {
        return new Queue("socketQueue", true);
    }


    @Bean
    public FanoutExchange socketFanoutExchange() {
        return new FanoutExchange("socketFanoutExchange");
    }

    @Bean
    public Binding bindFanoutExchange() {
        return BindingBuilder.bind(socketQueue()).to(socketFanoutExchange());
    }



}
