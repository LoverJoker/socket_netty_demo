package com.jokerliang.socket_netty_demo.device;

import lombok.Data;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-11 10:52
 */
@Data
public class DeviceDeal {

    private DeviceDeal(){
    }

    private String head = "AA";

    private String length;

    private String index = "01";

    private String cmd;

    private String data;

    private String check;

    private String end = "DD";




    private String getLength() {
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

    private String getCommand(String cmd, String data) {
        setCmd(cmd);
        setData(data);

        StringBuilder command = new StringBuilder();
        command.append(head);
        command.append(getLength());
        command.append(index);
        command.append(cmd);
        if (data != null && !data.equals("") && !data.equals("00")) {
            command.append(data);
        }
        command.append(getBCC());
        command.append(end);
        return command.toString();
    }

    private String getBCC() {
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

    private static String decodeData(String cmd) {
        cmd = cmd.replace(" ", "");
        return cmd.substring(8, cmd.length() - 4);
    }



    public static void main(String[] args) {
        String s = Command.query();
        System.out.println(s);
    }



    public static class Type {
        private Type(){}

        // 开机查询
        public final static String QUERY_DEVICE = "01";
        // 主板主动上传状态
        public final static String DEVICE_UPDATE_STATUS = "CC";
        // 大文件下载
        public final static String DOWN = "CD";
    }

    public static class Command {
        private Command(){}

        public static String query() {
            DeviceDeal deviceDeal = new DeviceDeal();
            return deviceDeal.getCommand(Type.QUERY_DEVICE, "00");
        }

        /**
         * 数据包下载
         * @return
         */
        public static String downUpdatePackage() {
            DeviceDeal deviceDeal = new DeviceDeal();
            return deviceDeal.getCommand(Type.DOWN, "");
        }

    }

    public static class Analysis {
        private Analysis(){}


        /**
         * 获取当前指令是干嘛的，对应协议的cmd 一栏
         * 具体的 Type 对应下面那个内部类
         * @param cmd
         * @return
         */
        public static String getType(String cmd) {
            cmd = cmd.replace(" ", "");
            return cmd.substring(6, 8);
        };


        /**
         * 如果是 查询反馈命令，那么解析出deviceCode
         * @param cmd
         * @return
         */
        public static String getDeviceCode(String cmd) {
            String data = decodeData(cmd);
            return data.substring(4);
        }

    }
}
