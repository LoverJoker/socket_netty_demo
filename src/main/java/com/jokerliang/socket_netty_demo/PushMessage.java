package com.jokerliang.socket_netty_demo;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:15
 */

public class PushMessage {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    @Override
    public String toString() {
        return "PushMessage{" +
                "content='" + content + '\'' +
                '}';
    }
}
