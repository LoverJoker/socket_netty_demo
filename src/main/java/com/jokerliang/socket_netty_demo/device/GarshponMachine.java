package com.jokerliang.socket_netty_demo.device;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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
    // 子设备Id, 写死01就可以了
    public static byte subDeviceId = 0X01;

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
     * 算长度
     * @param objects
     * @return
     */
    public static byte getLength(Object... objects) {
        // 默认需要有一个check的长度
        int length = 1;
        for (Object object : objects) {
            if (object instanceof byte[]) {
                length += ((byte[]) object).length;
            } else {
                length += 1;
            }
        }

        String s = Integer.toHexString(length);
        return ByteUtils.hexStr2Byte(s)[0];
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
            ArrayUtils.reverse(byteData);
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

    public static class CommandType {
        public final static byte DOWN = (byte) 0XCD;
        public final static byte QUERY = (byte) 0X01;
        public final static byte NORMAL = (byte) 0XCC;

        public final static byte ERROR_REPLAY = 0X13;

        public final static byte PARAM_VOLUME = 0X05;
        public final static byte PARAM_VOLUME_SET = 0X06;

        public final static byte SUB_STATUS = 0X01;

        public final static byte SUB_APPLY_PAY = 0X03;
        public final static byte SUB_APPLY_POINT = 0X04;
        public final static byte SUB_REPLAY_POINT_RESULT = 0X05;

        public final static byte SUB_QUERY_SPACE = 0X06;
        public final static byte SUB_SET_SPACE = 0X07;


        public final static byte SUB_BILL = 0X08;

        public static byte getType(byte[] command) {
            return command[3];
        }

        /**
         * 获取子命令
         * @return
         */
        public static byte getSubType(byte[] command) {
            return command[5];
        }
    }

    /**
     * 固件升级相关
     */
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
                String downFileName = "NDJ_DCW_V1.0.4.bin";
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
                int flength = 1 + 1 + fileName.length + fileSize.length + packetNum.length + packetSum.length + dataLength.length + fileData.length;
                byte[] frameLength = getWord(flength);
                byte[] bccCheck = getBCCCheck(length, index, cmd, frameLength, subCommand, nameLength, fileName, fileSize, packetSum, packetNum, dataLength, fileData);
                byte[] command = getCommand(head, length, index, cmd, frameLength, subCommand, nameLength, fileName,
                        fileSize, packetSum, packetNum, dataLength, fileData, bccCheck, end);

//                log.info("length: " + ByteUtils.byteToHex(length));
//                log.info("nameLength: " + ByteUtils.byteToHex(nameLength));
//                log.info("fileName: " + ByteUtils.byteArrayToHexString(fileName));
//                log.info("fileSize: " + ByteUtils.byteArrayToHexString(fileSize));
//                log.info("packageSum: " + ByteUtils.byteArrayToHexString(packetSum));
//                log.info("packetNum: " + ByteUtils.byteArrayToHexString(packetNum));
//                log.info("dataLength: " + ByteUtils.byteArrayToHexString(dataLength));
//                log.info("frameLength: " + ByteUtils.byteArrayToHexString(frameLength));
//                log.info("fileData: " + ByteUtils.byteArrayToHexString(fileData));
//                log.info("check: " + ByteUtils.byteArrayToHexString(bccCheck));
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
            byte length = getLength(index, cmd);
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

    /**
     * 主办主动上传状态
     */
    public static class Status{
        static byte cmd = CommandType.NORMAL;

        public static byte STATUS_OK = 0X00;
        public static byte STATUS_ERROR = 0X01;
        public static byte STATUS_OFFLINE = 0X02;
        public static byte STATUS_OTHER = 0X03;

        /**
         * 扭蛋机主动上传状态的服务器回波
         * @param allSpace 总参数
         * @return
         */
        public static byte[] backStatusToDevice(byte allSpace) {
            byte subCmd = CommandType.SUB_STATUS;
            byte length = getLength(index, cmd, subDeviceId, subCmd, allSpace);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId, subCmd, allSpace);
            return getCommand(head, length, index, cmd, subDeviceId, subCmd, allSpace, bccCheck, end);
        }

        /**
         * 获取总仓数
         * @param command
         * @return
         */
        public static byte getAllSpace(byte[] command) {
            return command[20];
        }


    }

    /**
     * 支付相关
     */
    public static class Pay{
        /**
         * 一次支付的全过程记录
         *    : ------申请支付开始:CBC3CA46AD4C
         *    : 申请支付的完整命令AA0C01CC010301CBC3CA46AD4CA7DD
         *    : 发送消息:AA0C01CC010301CBC3CA46AD4CA7DD
         *    : 接收到消息: AA0D02CC010301CBC3CA46AD4C00A5DD
         *    : 拼完后的完整消息AA0D02CC010301CBC3CA46AD4C00A5DD
         *    : 当前是申请支付命令
         *    : 当前仓位仓位号01状态:00此单订单号CBC3CA46AD4C
         *    : 云上分AA0E01CC010401CBC3CA46AD4C0100A3DD
         *    : 发送消息:AA0E01CC010401CBC3CA46AD4C0100A3DD
         *    : 接收到消息: AA0E02CC010401CBC3CA46AD4C0100A0DD
         *    : 拼完后的完整消息AA0E02CC010401CBC3CA46AD4C0100A0DD
         *    : 当前是云上分命令
         *    : 当前上分数量转化后的str:0001-当前订单号：CBC3CA46AD4C
         *    : 接收到消息: AA0E02CC010501CBC3CA46AD4C0100A1DD
         *    : 拼完后的完整消息AA0E02CC010501CBC3CA46AD4C0100A1DD
         *    : 当前是云上分上传结果
         *    : 云上分结果反馈命令:AA0C01CC010501CBC3CA46AD4CA1DD
         *    : 发送消息:AA0C01CC010501CBC3CA46AD4CA1DD
         */

        // Index + CMD +Data + Check 数据总长
        static byte cmd = CommandType.NORMAL;

        /**
         * 获取仓位状态
         * @param command
         * @return
         */
        public static byte getSpaceStatus(byte[] command) {
            return command[13];
        }

        /**
         * 获取仓位号
         * @param command
         * @return
         */
        public static byte getSpace(byte[] command) {
            return command[6];
        }

        /**
         * 获取订单号，从命令中
         * @param command
         * @return
         */
        public static byte[] getOrderCode(byte[] command) {
            return subData(command, 7, 13);
        }

        /**
         * 申请支付
         * @param orderCode 订单号 6个字节
         * @return
         */
        public static byte[] applyPay(byte[] orderCode) {
            byte space = 0X01;
            byte subCmd = CommandType.SUB_APPLY_PAY;
            byte length = getLength(index, cmd, subDeviceId, subCmd, space, orderCode);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId, subCmd, space, orderCode);
            byte[] command = getCommand(head, length, index, cmd, subDeviceId, subCmd, space, orderCode, bccCheck, end);
            log.info("申请支付的完整命令" + ByteUtils.byteArrayToHexString(command));
            return command;
        }


        /**
         * 云上分
         * @param space 仓位号
         * @param orderCode 订单号
         * @param point 上分数量
         * @return
         */
        public static byte[] applyPoint(byte space, byte[] orderCode, int point) {
            byte[] pointWord = getWord(point);
            byte subCmd = CommandType.SUB_APPLY_POINT;
            byte length = getLength(index, cmd, subDeviceId, subCmd, space, orderCode, pointWord);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId, subCmd, space, orderCode, pointWord);
            byte[] command = getCommand(head, length, index, cmd, subDeviceId, subCmd, space, orderCode, pointWord, bccCheck, end);
            log.info("云上分" + ByteUtils.byteArrayToHexString(command));
            return command;
        }

        /**
         * 从云上分命令获取上分数量，如果是0000就要退款
         * @param command
         * @return
         */
        public static byte[] getPointNumber(byte[] command) {
            return subData(command, 13, 15);
        }

        /**
         * 云上分结果回波
         * @param allSpace 仓位号
         * @param orderCode 订单号
         * @return
         */
        public static byte[] replayPointResult(byte allSpace, byte[] orderCode) {
            byte subCmd = CommandType.SUB_REPLAY_POINT_RESULT;
            byte length = getLength(index, cmd, subDeviceId, subCmd, allSpace, orderCode);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId, subCmd, allSpace, orderCode);
            byte[] command = getCommand(head, length, index, cmd, subDeviceId, subCmd, allSpace, orderCode, bccCheck, end);
            log.info("云上分结果反馈命令:" + ByteUtils.byteArrayToHexString(command));
            return command;
        }

        public static byte[] getOrderCode() {
            String nanoTime = Long.toHexString(System.nanoTime());
            byte[] bytes = ByteUtils.hexStr2Byte(nanoTime);
            ArrayUtils.reverse(bytes);
            return ArrayUtils.subarray(bytes, 0, 6);
        }

        public static String getOrderCodeStr(byte[] orderCode) {
            return ByteUtils.byteArrayToHexString(orderCode);
        }

        public static String getOrderCodeStr() {
            return ByteUtils.byteArrayToHexString(getOrderCode());
        }
    }

    /**
     * 仓位相关
     */
    public static class Space{

        static byte cmd = CommandType.NORMAL;

        /**
         * 查询仓位参数
         * @return
         */
        public static byte[] querySpace() {
            byte space = 0X01;
            byte subCmd = CommandType.SUB_QUERY_SPACE;
            byte length = getLength(index, cmd, subDeviceId, subCmd, space);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId, subCmd, space);
            return getCommand(head, length, index, cmd, subDeviceId, subCmd, space, bccCheck, end);
        }

        /**
         * 获取仓位状态
         * @param command
         * @return 是否在线
         */
        public static boolean getStatus(byte[] command) {
            byte b = command[7];
            return b == 0X00;
        }

        /**
         * 通过查询仓位指令得到仓位
         * @return
         */
        public byte getSpaceForCommand(byte[] command) {
            return command[6];
        }

        /**
         * 设置仓位号
         * 如果服务端和终端不同，以终端的为准
         * @param space 仓位
         * @param status 状态 0在线 1离线
         * @return
         */
        public byte[] setSpace(byte space, byte status) {
            byte subCommand = CommandType.SUB_SET_SPACE;
            byte length = getLength(index, cmd, subDeviceId, subCommand, space, status);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId, subCommand, space, status);
            return getCommand(head, length, index, cmd, subDeviceId, subCommand, space, status, bccCheck, end);
        }

    }


    /**
     * 故障相关
     */
    public static class Error {

        static byte cmd = CommandType.ERROR_REPLAY;

        /**
         * 获取故障代码
         * @param command
         * @return
         */
        public static byte getErrorCode(byte[] command) {
            return command[5];
        }

        public static String getErrorCodeExplain(byte errorCode) {
            switch (errorCode) {
                case 0x00:
                    return "无故障";
                case 0x01:
                    return "系统故障";
                case 0x02:
                    return "投币器故障";
                case 0x03:
                    return "卡扭蛋故障";
                case 0x04:
                    return "无扭蛋故障";
                case 0x05:
                    return "扭蛋库存不足";
            }
            return "未知故障：" + ByteUtils.byteToHex(errorCode);
        }

        /**
         * 设备故障上报应答
         */
        public static byte[] replayError() {
            byte length = getLength(index, cmd);
            byte[] bccCheck = getBCCCheck(length, index, cmd);
            return getCommand(head, length, index, cmd, bccCheck, end);
        }
    }


    /**
     * 上传账目增量，我方暂时不需要，给个回波
     */
    public static class Bill {

        static byte cmd = CommandType.NORMAL;


        public static byte[] replayBill(byte space, byte[] orderCode) {
            byte subCmd = CommandType.SUB_BILL;
            byte length = getLength(index, cmd, subDeviceId, subCmd, space, orderCode);
            byte[] bccCheck = getBCCCheck(length, index, cmd, subDeviceId
                    , subCmd, space, orderCode);
            return getCommand(head, length, index, cmd, subDeviceId, subCmd,
                    space, orderCode, bccCheck, end);
        }
    }

    /**
     * 扩展参数
     * 暂时只能修改音量
     */
    public static class Params {

        public static byte[] queryVolume() {
            byte cmd = CommandType.PARAM_VOLUME;
            byte length = getLength(index, cmd);
            byte[] bccCheck = getBCCCheck(length, index, cmd);
            return getCommand(head, length, index, cmd, bccCheck, end);
        }

        public static int getVolume(byte[] command) {
            byte volume = command[4];
            return Integer.parseInt(ByteUtils.byteToHex(volume), 16);
        }

        /**
         * 0-15 0-F
         * @param volume
         * @return
         */
        public static byte[] setVolume(int volume) {
            volume = Math.min(15, volume);
            String s = Integer.toHexString(volume);
            byte volumeByte = ByteUtils.hexStr2Byte(s)[0];
            byte cmd = CommandType.PARAM_VOLUME_SET;
            byte length = getLength(index, cmd, volumeByte);
            byte[] bccCheck = getBCCCheck(length, index, cmd, volumeByte);
            return getCommand(head, length, index, cmd, volumeByte, bccCheck, end);
        }
    }





}
