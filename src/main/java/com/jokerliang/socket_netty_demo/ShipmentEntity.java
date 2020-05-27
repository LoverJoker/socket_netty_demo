package com.jokerliang.socket_netty_demo;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 16:18
 */
public class ShipmentEntity {
    /**
     * 订单号
     */
    private String orderCode;

    /**
     * 出货数量
     */
    private int number;

    public ShipmentEntity(String orderCode, int number) {
        this.orderCode = orderCode;
        this.number = number;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
