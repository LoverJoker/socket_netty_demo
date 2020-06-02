package com.jokerliang.socket_netty_demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-02 12:19
 */
@Configuration
public class RedisConfig {

    @Bean
    MessageListenerAdapter listenerAdapter(RedisListener receiver) {
        return new MessageListenerAdapter(receiver, "onMessage");
    }


    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 可以添加多个 messageListener，配置不同的交换机
        container.addMessageListener(listenerAdapter, new PatternTopic("channel:test"));
        return container;
    }


    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
