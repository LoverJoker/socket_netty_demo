package com.jokerliang.socket_netty_demo.socket;

import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ServerMessageHandler extends IoHandlerAdapter {

    private static final ConcurrentHashMap<String, IoSession> clientMap = new ConcurrentHashMap<>();


    private static final String DEVICE_CODE_FILED_NAME = "deviceCode";

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
        sendMessage(session, "AA03010103DD");
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        IoBuffer inBuf = (IoBuffer) message;
        byte[] inbytes = new byte[inBuf.limit()];
        inBuf.get(inbytes, 0, inBuf.limit());
        String result = new String(inbytes, StandardCharsets.UTF_8);

        //
        if (true) {
            String deviceCode = result;
            session.setAttribute("deviceCode", DEVICE_CODE_FILED_NAME);
            clientMap.remove(deviceCode);
            clientMap.put(deviceCode, session);
        }


        log.info("接收到消息: " + result);

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

    private static void sendMessage(IoSession session, String message) {
        log.info("发送消息:" + message);
        byte[] responseByteArray = message.getBytes(StandardCharsets.UTF_8);
        IoBuffer responseIoBuffer = IoBuffer.allocate(responseByteArray.length);
        responseIoBuffer.put(responseByteArray);
        responseIoBuffer.flip();
        session.write(responseIoBuffer);
    }


    @Override
    public void sessionClosed(IoSession session) throws Exception {   //用户从服务器断开
        log.info("[服务断开]" + session.getId());
        String deviceCode = (String) session.getAttribute(DEVICE_CODE_FILED_NAME);
        clientMap.remove(deviceCode);
    }

    @Override
    public void messageSent(IoSession session, Object message){ //发送消息结束
        log.info("[发送消息结束]" + session.getId() + "message" + message);
    }

    /**
     * 这个方法在IoSession 的通道进入空闲状态时调用
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status)throws Exception {//重连
        // log.info("[服务重连]" + session.getId() + "status" + status.toString());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        log.error("服务异常" + session.getId());
        cause.printStackTrace();
    }



}
