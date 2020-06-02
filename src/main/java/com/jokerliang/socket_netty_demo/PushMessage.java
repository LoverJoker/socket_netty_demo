package com.jokerliang.socket_netty_demo;

import lombok.Data;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:15
 */
@Data
public class PushMessage {

    public static final String EVENT_SHIPMENT = "shipment";
    /**
     * 表示当前事件类型
     */
    private String event;

    /**
     * content,Json形式
     */
    private String content;

    private String deviceCode;

    public PushMessage(String deviceCode, String event, String content) {
        this.event = event;
        this.content = content;
        this.deviceCode = deviceCode;
    }


}
