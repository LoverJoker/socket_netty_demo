package com.jokerliang.socket_netty_demo.device;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-06-14 11:07
 */
@Slf4j
public class GarshponMachine {
    public static byte head = (byte) 0XAA;
    public static byte index = (byte) 0X01;
    public static byte end = (byte) 0XDD;


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

    public static byte[] toPrimitives(Byte[] oBytes){
        byte[] bytes = new byte[oBytes.length];

        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }

    public static byte[] getBCCCheck(Object... objects) {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Byte) {
                bytes.add((byte)object);
            }
            if (object instanceof byte[]) {
                byte[] bs = (byte[]) object;
                for (byte b : bs) {
                    bytes.add(b);
                }
            }
        }

        byte[] bys = ArrayListToByteArray(bytes);
        return getBCC(bys);
    }

    public static ArrayList<Byte> addToByteArray(ArrayList<Byte> destination, byte[] bytes) {
        for (byte aByte : bytes) {
            destination.add(aByte);
        }
        return destination;
    }

    public static byte[] ArrayListToByteArray(ArrayList<Byte> source) {
        Byte[] bytes = source.toArray(new Byte[source.size()]);
        return toPrimitives(bytes);
    }

    public static byte[] getCommand(Object... objects) {
        ArrayList<Byte> bytes = new ArrayList<>();

        for (Object object : objects) {
            if (object instanceof Byte) {
                bytes.add((Byte) object);
            }

            if (object instanceof byte[]) {
                byte[] bs = (byte[]) object;
                for (byte b : bs) {
                    bytes.add(b);
                }
            }
        }
        byte[] bys = ArrayListToByteArray(bytes);
        return bys;
    }

    /**
     * 如果是单字节需要补充到双字节
     * @return
     */
    public static byte[] getWord(byte[] byteData) {
        if (byteData.length < 2) {
            byte[] bytes = new byte[2];
            bytes[0] = byteData[0];
            bytes[1] = 0X00;
            return bytes;
        } else {
            return byteData;
        }
    }

    /**
     * 这个传进来的是10进制
     * @param data
     * @return
     */
    public static byte[] getWord(int data) {
        String s = Integer.toHexString(data);
        byte[] bytes = ByteUtils.hexStr2Byte(s);
        return getWord(bytes);
    }

    public static byte[] getWord(long data) {
        String s = Long.toHexString(data);
        byte[] bytes = ByteUtils.hexStr2Byte(s);
        return getWord(bytes);
    }

    public static class Update {

        /**
         * 数据包下载
         */
        public static ArrayList<byte[]> down() throws IOException {
            ArrayList<byte[]> returnByte = new ArrayList<>();

            String downFileName = "NDJ_DCW_V1.0.0.bin";
            File sourceFile = new File("src/main/resources/" + downFileName);
            String fileNameStr = downFileName.substring(0, downFileName.lastIndexOf("."));
            LinkedList<byte[]> fileBytes = FileSplitUtils.split(sourceFile, 512);

            byte cmd = CommandType.DOWN;
            byte subCommand = 0X01;
            byte nameLength = (byte) fileNameStr.length();
            byte[] fileName = fileNameStr.getBytes();
            byte[] fileSize = getWord(sourceFile.length());
            byte[] packetSum = getWord((sourceFile.length()%512==0?sourceFile.length()/512 :sourceFile.length()/512+1));
            byte length = (byte) 0XFF;

            for (int i = 0; i < fileBytes.size(); i++) {
//                byte[] fileData = new byte[1];
//                fileData[0] = 0x01;
                byte[] fileData = fileData = fileBytes.get(i);
                byte[] packetNum = getWord((i + 1));
                byte[] dataLength = getWord(fileData.length);
                byte[] frameLength = getWord((2 + 1 + 1 + fileName.length + 2 + 2 + 2 + 2 + fileData.length + 1));
                byte[] bccCheck = getBCCCheck(length, index, cmd, frameLength, subCommand, nameLength, fileSize, packetSum, packetNum, dataLength, fileData);
                byte[] command = getCommand(head, length, index, cmd, frameLength, subCommand, nameLength, fileName,
                        fileSize, packetSum, packetNum, dataLength, fileData, bccCheck, end);

                if (i == 0 ) {

                    log.info("nameLength: " + ByteUtils.byteToHex(nameLength));
                    log.info("fileName: " + ByteUtils.byteArrayToHexString(fileName));
                    log.info("fileSize: " + ByteUtils.byteArrayToHexString(fileSize));
                    log.info("packageSum: " + ByteUtils.byteArrayToHexString(packetSum));
                    log.info("packetNum: " + ByteUtils.byteArrayToHexString(packetNum));
                    log.info("dataLength: " + ByteUtils.byteArrayToHexString(dataLength));
                    log.info("frameLength: " + ByteUtils.byteArrayToHexString(frameLength));
                    log.info("fileData: " + ByteUtils.byteArrayToHexString(fileData));
                    log.info("check: " + ByteUtils.byteArrayToHexString(bccCheck));
                    log.info("完整的command: " + ByteUtils.byteArrayToHexString(command));


                }
                returnByte.add(command);
            }
            return returnByte;
        }
    }

    public static class Query {

        public static byte[] query() {
            byte cmd = 0x01;
            byte length = 0x03;
            byte[] bccCheck = getBCCCheck(length, index, cmd);
            byte[] command = getCommand(head, length, index, cmd, bccCheck, end);
            String s = ByteUtils.byteArrayToHexString(command);
            System.out.println(s);
            return command;
        }

        public static String getDeviceCodeFormCommand(byte[] command) {
            ArrayList<Byte> bbb = new ArrayList<>();

            for (int i = 0; i < command.length; i++) {
                if (i > 6-1 && i <= 18-1) {
                    bbb.add(command[i]);
                }
            }

            byte[] bytes1 = ArrayListToByteArray(bbb);
            return ByteUtils.byteArrayToHexString(bytes1);
        }

//        public static void getDeviceCodeFormCommand(byte[] command) {
//            ArrayList<Byte> bbb = new ArrayList<>();
//
//            for (int i = 0; i < command.length; i++) {
//                if (i > 6-1 && i <= 18-1) {
//                    bbb.add(command[i]);
//                }
//            }
//
//            byte[] bytes1 = ArrayListToByteArray(bbb);
//        }
    }

    public static class CommandType {
        public final static byte DOWN = (byte) 0XCD;
        public final static byte QUERY = (byte) 0X01;

        public static byte getType(byte[] command) {
            return command[3];
        }
    }


    public static void main(String[] args) throws IOException {
        Update.down();

    }

//    private static HashMap<String, byte[]> clientMessage = new HashMap<>();
//    public static void main(String[] args) throws IOException {
//       //GarshponMachine.Update.down();
//        //byte type = CommandType.getType(ByteUtils.hexStr2Byte("AA110201D90F48FF6D068065575226480867F9DD".trim()));
////         GarshponMachine.Query.getDeviceCodeFormCommand("AA 11 02 01 D90F 48FF6D068065575226480867 F9DD");
//        byte[] test = ByteUtils.hexStr2Byte("AA110201D90F48FF6D0680655752");
//        // 26480867F9DD
//
//        // 先判断这个命令是不是以DD结尾
//        String s = ByteUtils.byteArrayToHexString(test);
//
//        // 判断是否是 AA 开头， DD结尾
//
//        if (s.startsWith("AA") && s.endsWith("DD")) {
//            // 表示这个是个完整命令
//            System.out.println(test);
//        } else if (s.startsWith("AA")) {
//            // 表示这只是个开头
//            // 存到cache里面, 如果是AA开头直接存就好
//            clientMessage.put("deviceCode", test);
//        } else if (s.endsWith("DD")) {
//            // 表示 这是个结尾
//            byte[] cache = clientMessage.get("deviceCode");
//            // 拼起来，并且清空cache
//            clientMessage.remove("deviceCode");
//
//            byte[] bytes = ArrayUtils.addAll(cache, test);
//            System.out.println(bytes);
//        } else {
//            // 如果既不是开头也不是结尾，那么直接拼起来
//
//        }
//
//    }
}
