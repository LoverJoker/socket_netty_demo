package com.jokerliang.socket_netty_demo;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:15
 */
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


    public PushMessage(String event, String content) {
        this.event = event;
        this.content = content;
    }

    public static String getEventShipment() {
        return EVENT_SHIPMENT;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
