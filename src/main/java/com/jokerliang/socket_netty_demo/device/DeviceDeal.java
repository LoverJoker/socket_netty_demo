package com.jokerliang.socket_netty_demo.device;

import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

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

    private static String decodeData(String cmd) {
        cmd = cmd.replace(" ", "");
        return cmd.substring(8, cmd.length() - 4);
    }

    private static String addZero(String n) {
        if (n.length() < 2) {
            return "0" + n;
        }
        return n;
    }


    public static void main(String[] args) throws Exception {
        Command.update1();
        //Command.query2();



    }


    public static String toBinary(int num, int digits) {
        String cover = Integer.toBinaryString(1 << digits).substring(1);
        String s = Integer.toBinaryString(num);
        return s.length() < digits ? cover.substring(s.length()) + s : s;
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

        public static void query2() {
            byte head = (byte) 0XAA;
            byte index = 0X01;
            byte cmd = 0X01;
            byte end = (byte) 0xDD;
            byte data = 0;
            byte length = 1 + 1 + 1;
            byte[] checkData = new byte[4];
            checkData[0] = length;
            checkData[1] = index;
            checkData[2] = cmd;
            checkData[3] = data;
            byte[] check = getBCC(checkData);

            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add(head);
            bytes.add(length);
            bytes.add(index);
            bytes.add(cmd);
            for (byte b : check) {
                bytes.add(b);
            }
            bytes.add(end);

            Byte[] bytes1 = bytes.toArray(new Byte[bytes.size()]);


            System.out.println(ByteUtils.byteArrayToHexString(toPrimitives(bytes1)));
        }


        public static byte[] toPrimitives(Byte[] oBytes){
            byte[] bytes = new byte[oBytes.length];

            for(int i = 0; i < oBytes.length; i++) {
                bytes[i] = oBytes[i];
            }

            return bytes;
        }


        public static void update() throws Exception {
            String downFileName = "NDJ_DCW_V1.0.0.bin";

            File file = new File("src/main/resources/" + downFileName);
            // 先切
            LinkedList<byte[]> bytes = FileSplitUtils.split(file, 512);

            String head = "AA";
            String length = "";
            String index = "01";
            String cmd = "CD";
            String frameLength = "";
            // 下面这里就算data了
            String subCmd = "01";
            String fileName = downFileName.substring(0, downFileName.lastIndexOf("."));
            String nameLength = fileName.length()+ "";
            String packageSum =  file.length()%512==0?file.length()/512 + "":file.length()/512+1 + "";
            String fileSize = file.length() + "";
            for (int i = 0; i< bytes.size(); i++) {
                byte[] byteData = bytes.get(i);
                String dataLength = byteData.length + "";
                String packetNum = i + 1 + "";
                String fileData = ByteUtils.byteArrayToHexString(byteData);

            }

        }

        public static void update1() throws IOException {
            String downFileName = "NDJ_DCW_V1.0.0.bin";
            File file = new File("src/main/resources/" + downFileName);
            String fileNames = downFileName.substring(0, downFileName.lastIndexOf("."));
            LinkedList<byte[]> bytes = FileSplitUtils.split(file, 512);


            byte head = (byte) 0XAA;
            byte index = (byte) 0X01;
            byte cmd = (byte) 0XCD;
            byte subCmd = (byte) 0X01;
            byte nameLength = (byte) fileNames.length();
            byte[] fileName = fileNames.getBytes();
            byte fileSize = (byte) file.length();
            byte packetSum = (byte) (file.length()%512==0?file.length()/512 :file.length()/512+1);

            for (int i = 0; i< bytes.size(); i++) {
                ArrayList<Byte> all = new ArrayList<>();
                byte[] fileData = bytes.get(i);
                byte packetNum = (byte) (i + 1);
                byte dataLength = (byte) fileData.length;


                byte length = (byte) 0XFF;
                byte frameLength = (byte) (2 + 1 + 1 + fileName.length + 2 + 2 + 2 + 2 + fileData.length + 1);
                //byte check = length + index + cmd + frameLength + subCmd + nameLength + fileName + fileSize + packetSum + packetNum + dataLength + fileData;
                ArrayList<Byte> checkByte = new ArrayList<>();
                checkByte.add(length);
                checkByte.add(index);
                checkByte.add(cmd);
                // 这里开始是 data
                checkByte.add(frameLength);
                checkByte.add(subCmd);
                checkByte.add(nameLength);
                for (byte b : fileName) {
                    checkByte.add(b);
                }
                checkByte.add(fileSize);
                checkByte.add(packetSum);
                checkByte.add(packetNum);
                checkByte.add(dataLength);
                for (byte b : fileData) {
                    checkByte.add(b);
                }
                byte[] check = getBCC(toPrimitives(checkByte.toArray(new Byte[checkByte.size()])));



                byte end = (byte) 0XDD;


                all.add(head);
                all.add(length);
                all.add(index);
                all.add(cmd);
                all.add(frameLength);
                all.add(subCmd);
                all.add(nameLength);
                for (byte b : fileName) {
                    all.add(b);
                }
                all.add(fileSize);
                all.add(packetSum);
                all.add(packetNum);
                all.add(dataLength);
                for (byte fileDatum : fileData) {
                    all.add(fileDatum);
                }

                for (byte b : check) {
                    all.add(b);
                }
                all.add(end);

                Byte[] bytes1 = all.toArray(new Byte[all.size()]);

                String s = ByteUtils.byteArrayToHexString(toPrimitives(bytes1));
                System.out.println(s);
                System.out.println("");
            }

            System.out.println(bytes.size());
        }

        public static byte[] getBCC(byte[] data) {

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
            byte[] bytes = ByteUtils.hexStr2Byte(ret);
            return bytes;
        }


        private static String getPacketSum(Long length) {
            int packageSum = 0;
            if (length % 500 == 0) {
                packageSum = (int) (length / 500);
            } else {
                packageSum = (int) (Math.floor(length / 500) + 1);
            }
            return packageSum + "";
        }

        /**
         * 3.1.2 应答主办主动上传状态
         * @return
         */
        public static String reBackHeat() {
            DeviceDeal deviceDeal = new DeviceDeal();

            return deviceDeal.getCommand(Type.DEVICE_UPDATE_STATUS, "00");
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
