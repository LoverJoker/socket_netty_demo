package com.jokerliang.socket_netty_demo.device;

import lombok.Data;


import java.util.HashMap;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-11 10:52
 */
@Data
public class DeviceDeal {
    private String head = "AA";

    private String length;

    private String index = "01";

    private String cmd;

    private String data;

    private String check;

    private String end = "DD";




    public String getLength() {
        String lengthStr = head + index + cmd;
        int l = lengthStr.length() / 2;
        if (data!= null && !data.equals("00")) {
            l += data.length() / 2;
        }
        if (l > 10) {
            return l + "";
        }
        return "0" + l;

    }

    public String getCommand(String cmd, String data) {
        setCmd(cmd);
        setData(data);

        StringBuilder command = new StringBuilder();
        command.append(head);
        command.append(getLength());
        command.append(index);
        command.append(cmd);
        if (data != null && !data.equals("")) {
            command.append(data);
        }
        command.append(getBCC());
        command.append(end);
        return command.toString();
    }

    public String getBCC() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getLength());
        stringBuilder.append(getIndex());
        stringBuilder.append(getCmd());
        stringBuilder.append(getData());
        byte[] data = ByteUtils.hexStr2Byte(stringBuilder.toString());

        String ret = "";
        byte BCC[] = new byte[1];
        for (int i = 0; i < data.length; i++) {
            BCC[0] = (byte) (BCC[0] ^ data[i]);
        }
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }

    public static String payData(String subId, String subCmd, String space, String orderCode) {
        return subId + subCmd + space + orderCode;
    }

    public static void main(String[] args) {
        DeviceDeal deviceDeal = new DeviceDeal();
        String command = deviceDeal.getCommand("CC",
                DeviceDeal.payData("00", "01", "01", "1234123"));
        System.out.println(command);
    }
}
