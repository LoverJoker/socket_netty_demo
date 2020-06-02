package com.jokerliang.socket_netty_demo;

import java.util.UUID;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:15
 */
public interface SocketIOService {

    //推送的事件
    public static final String PUSH_EVENT = "push_event";
    public static final String RESPONSE_EVENT = "response_event";

    // 启动服务
    void start() throws Exception;

    // 停止服务
    void stop();

    // 推送信息
    SocketPushResult pushMessage(PushMessage pushMessage);
}
