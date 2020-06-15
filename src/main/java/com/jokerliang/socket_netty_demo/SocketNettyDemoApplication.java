package com.jokerliang.socket_netty_demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.netty.NettyRSocketServer;

@SpringBootApplication
public class SocketNettyDemoApplication {



    public static void main(String[] args) {
        SpringApplication.run(SocketNettyDemoApplication.class, args);


    }

}
