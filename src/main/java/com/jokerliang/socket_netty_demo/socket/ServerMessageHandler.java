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

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;


import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Query;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.CommandType;
import static com.jokerliang.socket_netty_demo.device.GarshponMachine.Update;

@Slf4j
@Component
public class ServerMessageHandler extends IoHandlerAdapter {


    private static final ConcurrentHashMap<String, IoSession> clientMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, byte[]> clientMessageCacheMap = new ConcurrentHashMap<>();


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
           clientMap.put("DD", session);
        // sendMessage(session, Query.query());

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
            switch (type) {
                case CommandType.QUERY:
                    String deviceCodeFromMachine = Query.getDeviceCodeFormCommand(command);
                    session.setAttribute(deviceCodeFromMachine, DEVICE_CODE_FILED_NAME);
                    clientMap.remove(deviceCodeFromMachine);
                    clientMap.put(deviceCodeFromMachine, session);
                    log.info("当前是查询命令，设备号是:" + deviceCodeFromMachine);
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
                case CommandType.NORMAL:
                    // 如果主命令是CC,就需要判断子命令
                    handlerNormalMessage(command);
                    break;
            }
        }


    }


    /**
     * 处理如果是0XCC 的命令, 需要判断子命令
     */
    public void handlerNormalMessage(byte[] command) {
        byte subType = CommandType.getSubType(command);
        switch (subType) {
            // 主板主动上传状态
            case CommandType.SUB_STATUS:
                log.info("当前是主板上传状态命令");
                break;
            // 申请支付
            case CommandType.SUB_APPLY_PAY:
                log.info("当前是申请支付命令");
                break;
        }
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
        session.write(IoBuffer.wrap(responseByteArray));
    }

    private static void sendMessage(IoSession session, byte[] data) {
        log.info("发送消息:" + ByteUtils.byteArrayToHexString(data));
        // byte[] responseByteArray = message.getBytes(StandardCharsets.UTF_8);
//        IoBuffer responseIoBuffer = IoBuffer.allocate(responseByteArray.length);
//        responseIoBuffer.put(responseByteArray);
//        responseIoBuffer.flip();
        session.write(IoBuffer.wrap(data));
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

}
