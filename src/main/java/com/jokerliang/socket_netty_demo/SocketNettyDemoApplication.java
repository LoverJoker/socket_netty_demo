package com.jokerliang.socket_netty_demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.netty.NettyRSocketServer;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class SocketNettyDemoApplication {



    public static void main(String[] args) {
        SpringApplication.run(SocketNettyDemoApplication.class, args);


    }

}
