package com.jokerliang.socket_netty_demo.device;

import com.jokerliang.socket_netty_demo.socket.GarshponServerMessageHandler;
import org.springframework.stereotype.Component;

import static com.jokerliang.socket_netty_demo.device.GarshponMachine.*;
/**
 * 求贤若饥 虚心若愚
 *  扭蛋机 控制出口
 * @author jokerliang
 * @date 2020-06-19 10:25
 */
@Component
public class GarsgponDeviceHandler {

    /**
     * 支付成功
     * @param deviceCode
     * @param orderCode 注意⚠️ orderCode 是 6 个字节
     */
    public void paySuccess(String deviceCode, String orderCode) {
        byte[] bytes = ByteUtils.hexStr2Byte(orderCode);
        byte[] payBytes = Pay.applyPay(bytes);
        GarshponServerMessageHandler.sendMessage(deviceCode, payBytes);
    }

    /**
     * 设置音量
     * @param deviceCode
     * @param volume 0 ～ 15
     */
    public void setVolume(String deviceCode, int volume) {
        byte[] bytes = Params.setVolume(volume);
        GarshponServerMessageHandler.sendMessage(deviceCode, bytes);
    }

}
