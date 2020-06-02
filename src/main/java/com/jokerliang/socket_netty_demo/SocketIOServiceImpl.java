package com.jokerliang.socket_netty_demo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

@Slf4j
@Service(value = "socketIOService")
public class SocketIOServiceImpl implements SocketIOService {

    @Autowired
    private Redisson redisson;


    @Autowired
    private SocketIOServer socketIOServer;

    /**
     * Spring IoC容器创建之后，在加载SocketIOServiceImpl Bean之后启动
     * @throws Exception
     */
    @PostConstruct
    private void autoStartup() throws Exception {
        start();
    }

    /**
     * Spring IoC容器在销毁SocketIOServiceImpl Bean之前关闭,避免重启项目服务端口占用问题
     * @throws Exception
     */
    @PreDestroy
    private void autoStop() throws Exception  {
        stop();
    }

    @Override
    public void start() {
        // 监听客户端连接
        socketIOServer.addConnectListener(client -> {
            String loginUserNum = getParamsByClient(client);
            log.info("客户端：" + loginUserNum + "已连接");
            UUID sessionId = client.getSessionId();

            log.info(sessionId.toString());
            log.info("==============");
            if (loginUserNum != null) {
               // UUID sessionId = client.getSessionId();
                System.out.println("============");
                System.out.println(sessionId);
            }
        });

        // 监听客户端断开连接
        socketIOServer.addDisconnectListener(client -> {
            String loginUserNum = getParamsByClient(client);
            if (loginUserNum != null) {
                client.disconnect();
            }
        });


        socketIOServer.start();

    }

    @Override
    public void stop() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            socketIOServer = null;
        }
    }

    @Override
    public SocketPushResult pushMessage(UUID clientUuid, PushMessage pushMessage) {

        SocketIOClient client = socketIOServer.getClient(clientUuid);
        if (client == null) {
            return new SocketPushResult(false, "客户端未连接");
        }

        AtomicBoolean isSuccess = new AtomicBoolean(false);
        // 处理自定义的事件，与连接监听类似
        socketIOServer.addEventListener(RESPONSE_EVENT, String.class, (subClient, data, ackSender) -> {
            isSuccess.set(true);
        });


        client.sendEvent(PUSH_EVENT, pushMessage);



        Callable<Boolean> callable = () -> {
            boolean flag = false;
            int i = 0;

            while (i < 5000 / 100) {
                Thread.sleep(100);
                i++;
                if(isSuccess.get()) {
                    flag = true;
                    break;
                }
            }

            return flag;
        };

        Future<Boolean> future = ThreadUtils.getInstance().getExecutorService().submit(callable);

        try {
            Boolean result = future.get();
            socketIOServer.removeAllListeners(RESPONSE_EVENT);
            return new SocketPushResult(result, result ? "推送成功" : "已发送消息，但是机器未给回波，此次服务器认为推送失败");
        } catch (Exception e) {
            e.printStackTrace();
            return new SocketPushResult(false, "推送发生异常");
        }
    }

    /**
     * 此方法为获取client连接中的参数，可根据需求更改
     * @param client
     * @return
     */
    private String getParamsByClient(SocketIOClient client) {
        // 从请求的连接中拿出参数（这里的loginUserNum必须是唯一标识）
        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
        List<String> list = params.get("deviceCode");
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
