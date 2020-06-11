package com.jokerliang.socket_netty_demo;


import com.jokerliang.socket_netty_demo.socket.ServerMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

@Configuration
@Slf4j
public class MinaConfig {
    private final int serverPort = 8768;

    @Autowired
    private ServerMessageHandler serverHandler;



    @Bean(destroyMethod = "unbind")
    public NioSocketAcceptor initMina() throws IOException {
        NioSocketAcceptor nioSocketAcceptor = new NioSocketAcceptor();
        nioSocketAcceptor.setHandler(serverHandler);
        nioSocketAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
//        KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactoryImpl();
//        KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory, IdleStatus.BOTH_IDLE);
//        heartBeat.setForwardEvent(true);
//        heartBeat.setRequestInterval(15 * 60);//心跳15分钟超时
//        nioSocketAcceptor.getFilterChain().addLast("heartbeat", heartBeat);
        nioSocketAcceptor.bind(new InetSocketAddress(serverPort));
        log.info("Mina启动了");
        return nioSocketAcceptor;
    }


    /**
     * 心跳超时处理器
     * @author tianfei
     *
     */
    private static class KeepAliveRequestTimeoutHandlerImpl implements KeepAliveRequestTimeoutHandler {

        @Override
        public void keepAliveRequestTimedOut(KeepAliveFilter filter,
                                             IoSession session) throws Exception {
            log.info("心跳超时！");
            session.close(true);
        }

    }
}
