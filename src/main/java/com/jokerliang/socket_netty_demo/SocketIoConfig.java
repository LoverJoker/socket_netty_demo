package com.jokerliang.socket_netty_demo;

import com.corundumstudio.socketio.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:02
 */
@Configuration
public class SocketIoConfig {


    @Bean
    public SocketIOServer socketIOServer() throws UnknownHostException {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setSocketConfig(socketConfig);

        config.setPort(8766);
        config.setBossThreads(1);
        config.setWorkerThreads(100);
        config.setAllowCustomRequests(true);
        config.setUpgradeTimeout(1000000);
        config.setPingTimeout(6000000);
        config.setPingInterval(25000);

        config.setTransports(Transport.POLLING, Transport.WEBSOCKET);
        config.setOrigin(":*:");

        return new SocketIOServer(config);
    }
}
