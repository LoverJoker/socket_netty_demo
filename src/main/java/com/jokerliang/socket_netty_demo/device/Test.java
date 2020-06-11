package com.jokerliang.socket_netty_demo.device;




import com.jokerliang.socket_netty_demo.device.ByteUtils;

import java.util.HashMap;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-11 11:00
 */
public class Test {

    public static String getBCC(HashMap<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(map.get("length"));
        stringBuilder.append(map.get("index"));
        stringBuilder.append(map.get("cmd"));
        stringBuilder.append(map.get("data"));
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

    public static void main(String[] args) {

        HashMap<String, String> map = new HashMap<>();
        map.put("head", "AA");
        map.put("index", "01");
        map.put("cmd", "01");
        map.put("length", getLength(map));
        map.put("data", "00");
        map.put("check", getBCC(map));
        map.put("end", "DD");
        System.out.println(map);

        String cmd = map.get("head") + map.get("length") + map.get("index") + map.get("cmd") + map.get("check") + map.get("end");

        System.out.println(cmd);

    }


    public static String getLength(HashMap<String, String> map) {
        String s = map.get("head") + map.get("index") + map.get("cmd");
        int i = s.length() / 2;
        String data = map.get("data");
        if (data!= null && !data.equals("00")) {
            i += data.length() / 2;
        }
        return "0" + i;
    }

}
