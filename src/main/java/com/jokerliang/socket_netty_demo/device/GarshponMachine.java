package com.jokerliang.socket_netty_demo.device;

import io.lettuce.core.protocol.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * 裁切数据
     * @param command
     * @param start
     * @param end
     * @return
     */
    public static byte[] subData(byte[] command, int start, int end) {
        return ArrayUtils.subarray(command, start, end);
    }

    /**
     * 倒着裁切, 注意 start 也是倒着来的，最后一个就是0
     * @param command
     * @param start
     * @param end
     * @return
     */
    public static byte[] lastSubData(byte[] command, int start, int end) {
        ArrayUtils.reverse(command);
        byte[] subarray = ArrayUtils.subarray(command, start, end);
        ArrayUtils.reverse(subarray);
        return subarray;
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
     * @param data 10进制
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
         * 这个main方法有所有参数的解析，做个备份
         *  public static void main(String[] args) throws IOException {
         *         int i = Integer.parseInt("0E", 16);
         *         System.out.println(i);
         * //       GarshponMachine.Update.down();
         *         //byte type = CommandType.getType(ByteUtils.hexStr2Byte("".trim()));
         *         String deviceCode = Query.getDeviceCodeFormCommand(ByteUtils.hexStr2Byte("AA110201D90F48FF6D068065575226480867F9DD"));
         *
         *         // 48FF6D068065575226480867
         *         System.out.println("AA 1A 02 CD 01 0E 4E444A5F4443575F56312E302E30 5068 2900 0100 02 BF DD");
         *
         *         // AA1A02CD010E4E444A5F4443575F56312E302E3050682900010002BFDD
         *         byte[] test = ByteUtils.hexStr2Byte("AA1A02CD010E4E444A5F4443575F56312E302E3050682900010002BFDD");
         *
         *         // AA1A02CD010E4E444A5F4443575F56312E302E3050682900 0100 02 BF DD
         *
         *         System.out.println("cmd:" + ByteUtils.byteArrayToHexString(subData(test, 3, 4)));
         *         System.out.println("subCmd:" + ByteUtils.byteArrayToHexString(subData(test, 4, 5)));
         *         byte[] nameLength = subData(test, 5, 6);
         *         System.out.println("nameLength:" + ByteUtils.byteArrayToHexString(nameLength));
         *         int nameLengthInt = Integer.parseInt(ByteUtils.byteArrayToHexString(nameLength), 16);
         *         byte[] fileNameData = subData(test, 6, 6 + nameLengthInt);
         *         System.out.println("fileName:" + ByteUtils.byteArrayToHexString(fileNameData));
         *         String s = new String(fileNameData, StandardCharsets.UTF_8);
         *         System.out.println(s);
         *         System.out.println("fileSize:" + ByteUtils.byteArrayToHexString(subData(test, 6 + nameLengthInt, 6 + nameLengthInt + 2)));
         *         System.out.println("packSum:" + ByteUtils.byteArrayToHexString(subData(test, 6 + nameLengthInt + 2, 6 + nameLengthInt + 2 + 2)));
         *         System.out.println("packNum:" + ByteUtils.byteArrayToHexString(subData(test, 6 + nameLengthInt + 2 + 2, 6 + nameLengthInt + 2 + 2 + 2)));
         *         System.out.println("fileResult:" + ByteUtils.byteArrayToHexString(subData(test, 6 + nameLengthInt + 2 + 2 + 2, 6 + nameLengthInt + 2 + 2 + 2 + 1)));
         *         System.out.println("check:" + ByteUtils.byteArrayToHexString(subData(test, 6 + nameLengthInt + 2 + 2 + 2 + 1, 6 + nameLengthInt + 2 + 2 + 2 + 1)));
         *
         *
         *         String fileResult = Update.getFileResult(test);
         *         System.out.println(fileResult);
         *     }
         */

        /**
         * 数据包下载
         */
//        public static ArrayList<byte[]> down() throws IOException {
//            ArrayList<byte[]> returnByte = new ArrayList<>();
//
//            String downFileName = "NDJ_DCW_V1.0.0.bin";
//            File sourceFile = new File("src/main/resources/" + downFileName);
//            String fileNameStr = downFileName.substring(0, downFileName.lastIndexOf("."));
//            LinkedList<byte[]> fileBytes = FileSplitUtils.split(sourceFile, 512);
//
//            byte cmd = CommandType.DOWN;
//            byte subCommand = 0X01;
//            byte nameLength = (byte) fileNameStr.length();
//            byte[] fileName = fileNameStr.getBytes();
//            byte[] fileSize = getWord(sourceFile.length());
//            byte[] packetSum = getWord((sourceFile.length()%512==0?sourceFile.length()/512 :sourceFile.length()/512+1));
//            byte length = (byte) 0XFF;
//
//            for (int i = 0; i < fileBytes.size(); i++) {
//                byte[] fileData = new byte[1];
//                fileData[0] = 0x01;
////                byte[] fileData  = fileBytes.get(i);
//                byte[] packetNum = getWord((i + 1));
//                byte[] dataLength = getWord(fileData.length);
//                byte[] frameLength = getWord(1 + 1 + fileName.length + fileSize.length + packetNum.length + packetSum.length + dataLength.length + fileData.length);
////                byte[] bccCheck = getBCCCheck(length, index, cmd, frameLength, subCommand, nameLength, fileSize, packetSum, packetNum, dataLength, fileData);
//                byte[] bccCheck = getBCCCheck(length, index, cmd, frameLength, subCommand, nameLength, fileName, fileSize, packetSum, packetNum, dataLength, fileData);
//                byte[] command = getCommand(head, length, index, cmd, frameLength, subCommand, nameLength, fileName,
//                        fileSize, packetSum, packetNum, dataLength, fileData, bccCheck, end);
//
//                if (i == 0 ) {
//
//                    log.info("nameLength: " + ByteUtils.byteToHex(nameLength));
//                    log.info("fileName: " + ByteUtils.byteArrayToHexString(fileName));
//                    log.info("fileSize: " + ByteUtils.byteArrayToHexString(fileSize));
//                    log.info("packageSum: " + ByteUtils.byteArrayToHexString(packetSum));
//                    log.info("packetNum: " + ByteUtils.byteArrayToHexString(packetNum));
//                    log.info("dataLength: " + ByteUtils.byteArrayToHexString(dataLength));
//                    log.info("frameLength: " + ByteUtils.byteArrayToHexString(frameLength));
//                    log.info("fileData: " + ByteUtils.byteArrayToHexString(fileData));
//                    log.info("check: " + ByteUtils.byteArrayToHexString(bccCheck));
//                    log.info("完整的command: " + ByteUtils.byteArrayToHexString(command));
//
//
//                }
//                returnByte.add(command);
//            }
//            return returnByte;
//        }

        /**
         * 数据包下载,获取第N帧数据
         * @param frameIndex 从1 开始！！
         * @return
         */
        public static byte[] getDownFrame(int frameIndex) {
            try {
                String downFileName = "NDJ_DCW_V1.0.0.bin";
                File sourceFile = new File("src/main/resources/" + downFileName);
                String fileNameStr = downFileName.substring(0, downFileName.lastIndexOf("."));
                LinkedList<byte[]> fileBytes = FileSplitUtils.split(sourceFile, 512);
                byte cmd = CommandType.DOWN;
                byte subCommand = 0X01;
                byte nameLength = (byte) fileNameStr.length();
                byte[] fileName = fileNameStr.getBytes();
                byte[] fileSize = getWord(sourceFile.length());
                byte[] packetSum = getWord((sourceFile.length() % 512 == 0 ? sourceFile.length() / 512 : sourceFile.length() / 512 + 1));
                byte length = (byte) 0XFF;
//                byte[] fileData = new byte[1];
//                fileData[0] = 0x01;
                byte[] fileData  = fileBytes.get(frameIndex - 1);
                byte[] packetNum = getWord((frameIndex - 1 + 1));
                byte[] dataLength = getWord(fileData.length);
                byte[] frameLength = getWord(1 + 1 + fileName.length + fileSize.length + packetNum.length + packetSum.length + dataLength.length + fileData.length);
                byte[] bccCheck = getBCCCheck(length, index, cmd, frameLength, subCommand, nameLength, fileName, fileSize, packetSum, packetNum, dataLength, fileData);
                byte[] command = getCommand(head, length, index, cmd, frameLength, subCommand, nameLength, fileName,
                        fileSize, packetSum, packetNum, dataLength, fileData, bccCheck, end);

                log.info("nameLength: " + ByteUtils.byteToHex(nameLength));
                log.info("fileName: " + ByteUtils.byteArrayToHexString(fileName));
                log.info("fileSize: " + ByteUtils.byteArrayToHexString(fileSize));
                log.info("packageSum: " + ByteUtils.byteArrayToHexString(packetSum));
                log.info("packetNum: " + ByteUtils.byteArrayToHexString(packetNum));
                log.info("dataLength: " + ByteUtils.byteArrayToHexString(dataLength));
                log.info("frameLength: " + ByteUtils.byteArrayToHexString(frameLength));
                log.info("fileData: " + ByteUtils.byteArrayToHexString(fileData));
                log.info("check: " + ByteUtils.byteArrayToHexString(bccCheck));
                log.info("下载完整的command: " + ByteUtils.byteArrayToHexString(command));


                return command;
            } catch (Exception e) {
                log.error("扭蛋机更新失败");
                e.printStackTrace();
                return new byte[1];
            }
        }

        public static String getFileResult(byte[] command) {
            byte[] nameLength = subData(command, 5, 6);
            int nameLengthInt = Integer.parseInt(ByteUtils.byteArrayToHexString(nameLength), 16);
            byte[] bytes = subData(command, 6 + nameLengthInt + 2 + 2 + 2, 6 + nameLengthInt + 2 + 2 + 2 + 1);
            return ByteUtils.byteArrayToHexString(bytes);
        }

        public static int getPacketNum(byte[] command) {
            byte[] nameLength = subData(command, 5, 6);
            int nameLengthInt = Integer.parseInt(ByteUtils.byteArrayToHexString(nameLength), 16);
            byte[] bytes = subData(command, 6 + nameLengthInt + 2 + 2, 6 + nameLengthInt + 2 + 2 + 2);
            ArrayUtils.reverse(bytes);
            return Integer.parseInt(ByteUtils.byteArrayToHexString(bytes), 16);
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
            byte[] bytes = subData(command, 6, 18);
            return ByteUtils.byteArrayToHexString(bytes);
        }

    }

    public static class CommandType {
        public final static byte DOWN = (byte) 0XCD;
        public final static byte QUERY = (byte) 0X01;

        public static byte getType(byte[] command) {
            return command[3];
        }
    }

    public static void main(String[] args) throws IOException {
        byte[] downFrame = Update.getDownFrame(1);
        System.out.println(ByteUtils.byteArrayToHexString(downFrame));
    }




}
