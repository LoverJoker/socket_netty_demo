package com.jokerliang.socket_netty_demo;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-28 09:19
 */
public class SocketPushResult {
    private boolean isSuccess;
    private String result;


    public SocketPushResult(boolean isSuccess, String result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
