package com.jokerliang.socket_netty_demo.socket;

import com.jokerliang.socket_netty_demo.ReceiveLog;
import com.jokerliang.socket_netty_demo.device.ByteUtils;

import com.jokerliang.socket_netty_demo.device.GarshponMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import javax.websocket.Session;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Query;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.CommandType;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Update;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Status;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Space;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Pay;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Error;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Bill;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Params;

@Slf4j
@Component
public class ServerMessageHandler extends IoHandlerAdapter {


    private static final ConcurrentHashMap<String, IoSession> clientMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, byte[]> clientMessageCacheMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> messageRetryMap = new ConcurrentHashMap<>();


    public static final String DEVICE_CODE_FILED_NAME = "deviceCode";

    @Override
    public void sessionCreated(IoSession session) throws Exception { //用户连接到服务器
        SocketSessionConfig cfg = (SocketSessionConfig) session.getConfig();
        cfg.setSoLinger(0);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);

        log.info("[服务建立]" + session.getId());
        // 服务建立后发送设备号的指令
        Thread.sleep(2000);
        sendMessage(session, Query.query());

    }

    /**
     * 得到最后的命令
     * 如果是分片的命令
     * 通过这个方法可以组合起来
     * @param command
     */
    private static byte[] composeCommand(byte[] command, String sessionId) {
        String s = ByteUtils.byteArrayToHexString(command);
        if (s.startsWith("AA") && s.endsWith("DD")) {
            // 表示这个是个完整命令
            return command;
        } else if (s.startsWith("AA")) {
            // 表示这只是个开头
            // 存到cache里面, 如果是AA开头直接存就好
            clientMessageCacheMap.put(sessionId, command);
            return null;
        } else if (s.endsWith("DD")) {
            // 表示 这是个结尾
            byte[] cache = clientMessageCacheMap.get(sessionId);
            // 拼起来，并且清空cache
            clientMessageCacheMap.remove(sessionId);

            byte[] bytes = ArrayUtils.addAll(cache, command);
            return bytes;
        } else {
            // 如果既不是开头也不是结尾，那么直接拼起来
            byte[] cache = clientMessageCacheMap.get(sessionId);
            byte[] bytes = ArrayUtils.addAll(cache, command);
            clientMessageCacheMap.remove(sessionId);
            clientMessageCacheMap.put(sessionId, bytes);
            return null;
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        IoBuffer inBuf = (IoBuffer) message;
        byte[] inbytes = new byte[inBuf.limit()];
        inBuf.get(inbytes, 0, inBuf.limit());
        String commandStr = ByteUtils.byteArrayToHexString(inbytes);
        log.info("接收到消息: " + commandStr);

        byte[] command = composeCommand(inbytes, session.getId() + "");

        if (command != null) {
            log.info("拼完后的完整消息" + ByteUtils.byteArrayToHexString(command));
            byte type = CommandType.getType(command);
            if (session.getAttribute(DEVICE_CODE_FILED_NAME) != null) {
                String deviceCode = (String) session.getAttribute(DEVICE_CODE_FILED_NAME);
                log.info("当前通过 session取得的设备号:" + deviceCode);
            }

            byte retryType = type;
            if (retryType == CommandType.NORMAL) {
                retryType = CommandType.getSubType(command);
            }

            messageRetryMap.remove(session.getId() + ByteUtils.byteToHex(retryType));


            switch (type) {
                case CommandType.QUERY:
                    String deviceCodeFromMachine = Query.getDeviceCodeFormCommand(command);
                    session.setAttribute(deviceCodeFromMachine, DEVICE_CODE_FILED_NAME);
                    clientMap.remove(deviceCodeFromMachine);
                    clientMap.put("DD", session);
//                    clientMap.put(deviceCodeFromMachine, session);
                    log.info("当前是查询命令，设备号是:" + deviceCodeFromMachine);
                    // 需要发送查询仓位参数
                    // byte[] querySpace = Space.querySpace();
                    // sendMessage(session, querySpace);

                    break;
                case CommandType.DOWN:
                    // 解析
                    int packetNum = Update.getPacketNum(command);
                    String fileResult = Update.getFileResult(command);
                    fileResult = fileResult.trim();
                    log.info("当前是下载命令,result = " + fileResult + "/packetNum=" + packetNum);
                    if (fileResult.equals("01")) {
                         byte[] downFrame = Update.getDownFrame(packetNum + 1);
                         sendMessage(session, downFrame);
                    } else if (fileResult.equals("02")) {
                        byte[] downFrame = Update.getDownFrame(packetNum);
                        sendMessage(session, downFrame);
                    }
                    break;
                case CommandType.ERROR_REPLAY:
                    // 当前是主动故障上报
                    byte errorCode = Error.getErrorCode(command);
                    if (errorCode != 0X00) {
                        String errorCodeExplain = Error.getErrorCodeExplain(errorCode);
                        log.info("当前是主动故障上报,故障代码是：" + ByteUtils.byteToHex(errorCode) + "对应解释：" + errorCodeExplain);
                    }
                    byte[] bytes = Error.replayError();
                    sendMessage(session, bytes);
                    break;
                case CommandType.PARAM_VOLUME:
                    int volume = Params.getVolume(command);
                    log.info("当前是获取音量指令：" + volume);
                    break;
                case CommandType.NORMAL:
                    // 如果主命令是CC,就需要判断子命令
                    handlerNormalMessage(session, command);
                    break;
            }
        }


    }


    /**
     * 处理如果是0XCC 的命令, 需要判断子命令
     */
    public void handlerNormalMessage(IoSession session, byte[] command) {
        byte subType = CommandType.getSubType(command);
        switch (subType) {
            // 主板主动上传状态
            case CommandType.SUB_STATUS:
                log.info("当前是主板上传状态命令");
                // AA 16 02 CC 01 01 8B00D564000000640000006C9700 01 01007DDD
                byte allSpace = Status.getAllSpace(command);
                byte[] bytes = Status.backStatusToDevice(allSpace);
                sendMessage(session, bytes);
                break;
            // 申请支付
            case CommandType.SUB_APPLY_PAY:
                log.info("当前是申请支付命令");
                // AA 0D 02 CC 01 03 01 CB218792D645 03 AFDD
                // 接收到这个指令后就需要上分命令
                byte spaceStatus = Pay.getSpaceStatus(command);
                byte space = Pay.getSpace(command);
                byte[] orderCode = Pay.getOrderCode(command);
                log.info("当前仓位仓位号" + ByteUtils.byteToHex(space)
                        + "状态:" + ByteUtils.byteToHex(spaceStatus)
                        + "此单订单号" + ByteUtils.byteArrayToHexString(orderCode));
                if (spaceStatus == Status.STATUS_ERROR || spaceStatus == Status.STATUS_OFFLINE) {
                    refund(orderCode);
                    log.info("当前仓位不允许出货");
                    return;
                }
                if (spaceStatus == Status.STATUS_OTHER) {
                    // 拿到该台机器最后一个订单号，然后发送 上分 0 解决。
                    // 每一次新订单，都应该要检查该台机器的最后一个订单是否已经完成。
                    // 如果没有完成，需要等他完成以后，再发送下一次申请支付。
                    return;
                }
                // 允许出货发送上分指令
                byte[] pointCommand = Pay.applyPoint(space, orderCode, 1);
                sendMessage(session, pointCommand);
                break;

            case CommandType.SUB_APPLY_POINT:
                log.info("当前是云上分命令");
                // AA0E02CC010401ED723576774A010024DD
                // AA 0E 02 CC 01 04 01 ED723576774A 0100 24 DD
                // 需要解析 上分数量判断是否要退款
                byte[] pointNumber = Pay.getPointNumber(command);
                byte[] pointOrderCode = Pay.getOrderCode(command);
                // 首先字节翻转
                ArrayUtils.reverse(pointNumber);
                // 转string判断
                String pointNumberStr = ByteUtils.byteArrayToHexString(pointNumber);
                log.info("当前上分数量转化后的str:" + pointNumberStr + "-当前订单号：" + ByteUtils.byteArrayToHexString(pointOrderCode));
                if (pointNumberStr.equals("0000")) {
                    // 两个字节需要退款
                    log.info("当前需要退款");
                    refund(pointOrderCode);
                }
             break;
            case CommandType.SUB_REPLAY_POINT_RESULT:
                // AA0E02CC010501ED723576774A010025DD
                log.info("当前是云上分上传结果");
                byte[] orderCode1 = Pay.getOrderCode(command);
                byte space1 = Pay.getSpace(command);
                byte[] bytes1 = Pay.replayPointResult(space1, orderCode1);
                sendMessage(session, bytes1);
                break;
            case CommandType.SUB_BILL:
                byte[] orderCodeBill = Pay.getOrderCode(command);
                byte spaceBill = Pay.getSpace(command);
                log.info("当前是上传账目增量订单号是：" + ByteUtils.byteArrayToHexString(orderCodeBill)
                        + "仓位是：" + ByteUtils.byteToHex(spaceBill));
                byte[] billByte = Bill.replayBill(spaceBill, orderCodeBill);
                sendMessage(session, billByte);
                break;
            case CommandType.SUB_QUERY_SPACE:
                // AA0702CC01060100CFDD
                boolean status = Space.getStatus(command);
                log.info("当前是仓位查询, 是否在线：" + status);
//                if (!status) {
//                    // 如果是离线，主动下发要他在线
//
//                }
                break;
        }
    }

    /**
     * 退款
     */
    public void refund(byte[] orderCode) {
        log.info("当前订单号需要退款: " + ByteUtils.byteArrayToHexString(orderCode));
    }

    public static Boolean sendMessage(String deviceCode, String message) {
        if (!clientMap.containsKey(deviceCode)) {
            log.info("设备：" + deviceCode + "未在此服务器上连接");
            return false;
        }
        IoSession session = clientMap.get(deviceCode);
        // 发送到客户端
        sendMessage(session, message);
        return true;
    }

    public static Boolean sendMessage(String deviceCode, byte[] message) {
        if (!clientMap.containsKey(deviceCode)) {
            log.info("设备：" + deviceCode + "未在此服务器上连接");
            return false;
        }
        IoSession session = clientMap.get(deviceCode);
        // 发送到客户端
        sendMessage(session, message);
        return true;
    }

    private static void sendMessage(IoSession session, String message) {
        log.info("发送消息:" + message);
        message = message.trim();
        byte[] responseByteArray = ByteUtils.hexStr2Byte(message);
        // byte[] responseByteArray = message.getBytes(StandardCharsets.UTF_8);
//        IoBuffer responseIoBuffer = IoBuffer.allocate(responseByteArray.length);
//        responseIoBuffer.put(responseByteArray);
//        responseIoBuffer.flip();
        sendMessage(session, responseByteArray);
    }


    private static void sendMessage(IoSession session, byte[] data) {
        log.info("发送消息:" + ByteUtils.byteArrayToHexString(data));
        session.write(IoBuffer.wrap(data));

        // 1s 后检测是否收到了回波，如果没有收到，就要重发

        byte type = CommandType.getType(data);
        if (type == CommandType.NORMAL) {
            type = CommandType.getSubType(data);
        }


        messageRetryMap.put(session.getId() + ByteUtils.byteToHex(type), true);

        retrySendMessage(0, type, data, session);
    }

    private static void retrySendMessage(int count, byte type, byte[] data, IoSession session) {
        count += 1;
        if (count > 4) {
            log.info("此条命令重试次数超过限制");
            return;
        }
        int finalCount = count;
        Observable
                .just(data)
                .delay(1, TimeUnit.SECONDS)
                .filter(new Func1<byte[], Boolean>() {
                    @Override
                    public Boolean call(byte[] bytes) {
                        Boolean aBoolean = messageRetryMap.get(session.getId() + ByteUtils.byteToHex(type));
                        return aBoolean != null && aBoolean;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .doOnNext(new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {
                        log.info("当前数据进行重试操作：Type:" + CommandType.getType(bytes) + "-数据："+ ByteUtils.byteArrayToHexString(bytes));
                        messageRetryMap.put(session.getId() + ByteUtils.byteToHex(type), true);
                        session.write(IoBuffer.wrap(bytes));
                        retrySendMessage(finalCount, type, data, session);
                    }
                })
                .subscribe();
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {   //用户从服务器断开

        String deviceCode = (String) session.getAttribute(DEVICE_CODE_FILED_NAME);
        log.info("[服务断开]" + deviceCode);
        clientMap.remove(deviceCode);
    }

    @Override
    public void messageSent(IoSession session, Object message){ //发送消息结束
        // log.info("[发送消息结束]" + session.getId() + "message" + message);
    }



    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        log.error("服务异常" + session.getId() + "//deviceCode" + session.getAttribute(DEVICE_CODE_FILED_NAME));
        cause.printStackTrace();
    }


    public static void main(String[] args) throws InterruptedException {
        Observable
                .just(1)
                .delay(1, TimeUnit.SECONDS)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return false;
                    }
                })
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.println(integer);
                    }
                }).subscribe();

        Thread.sleep(20000);
    }
}
