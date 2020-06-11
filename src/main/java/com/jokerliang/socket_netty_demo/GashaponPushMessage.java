package com.jokerliang.socket_netty_demo;

import lombok.Data;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-10 14:56
 */
@Data
public class GashaponPushMessage {
    private String deviceCode;
    private String message;

    public static String getJson(String deviceCode, String message) {
        GashaponPushMessage gashaponPushMessage = new GashaponPushMessage();
        gashaponPushMessage.setDeviceCode(deviceCode);
        gashaponPushMessage.setMessage(message);
        return GsonUtil.getGson().toJson(gashaponPushMessage);
    }

    public static GashaponPushMessage fromJson(String json) {
        return GsonUtil.getGson().fromJson(json, GashaponPushMessage.class);
    }
}
