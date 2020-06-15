package com.jokerliang.socket_netty_demo.socket;

import com.jokerliang.socket_netty_demo.ReceiveLog;
import com.jokerliang.socket_netty_demo.device.ByteUtils;
import com.jokerliang.socket_netty_demo.device.DeviceDeal;
import com.jokerliang.socket_netty_demo.device.GarshponMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static com.jokerliang.socket_netty_demo.device.DeviceDeal.Type;
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
        sendMessage(session, Query.query());

    }


    @Override
    public void messageReceived(IoSession session, Object message) {
        IoBuffer inBuf = (IoBuffer) message;
        byte[] inbytes = new byte[inBuf.limit()];
        inBuf.get(inbytes, 0, inBuf.limit());
        String commandStr = ByteUtils.byteArrayToHexString(inbytes);

        byte type = CommandType.getType(inbytes);

        if (session.getAttribute(DEVICE_CODE_FILED_NAME) != null) {
            String deviceCode = (String) session.getAttribute(DEVICE_CODE_FILED_NAME);
            log.info("当前通过 session取得的设备号:" + deviceCode);
        }
        switch (type) {
            case CommandType.QUERY:
                String deviceCodeFromMachine = Query.getDeviceCodeFormCommand(inbytes);
                session.setAttribute(deviceCodeFromMachine, DEVICE_CODE_FILED_NAME);
                clientMap.remove(deviceCodeFromMachine);
                clientMap.put(deviceCodeFromMachine, session);
                log.info("当前是查询命令，设备号是:" + deviceCodeFromMachine);
                break;
            case CommandType.DOWN:
                log.info("当前是下载命令");
                break;
        }




        log.info("接收到消息: " + commandStr);

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
        log.info("[服务断开]" + session.getId());
        String deviceCode = (String) session.getAttribute(DEVICE_CODE_FILED_NAME);
        clientMap.remove(deviceCode);
    }

    @Override
    public void messageSent(IoSession session, Object message){ //发送消息结束
       // log.info("[发送消息结束]" + session.getId() + "message" + message);
    }



    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        log.error("服务异常" + session.getId());
        cause.printStackTrace();
    }

}
