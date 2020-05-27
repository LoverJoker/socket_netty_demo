package com.jokerliang.socket_netty_demo;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 16:50
 */
public class ResponseMessage {

    public static final int SUCCESS = 1;

    private int status;


    public ResponseMessage(int status) {
        this.status = status;
    }

    public static int getSUCCESS() {
        return SUCCESS;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

