package com.jokerliang.socket_netty_demo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import rx.Observable;
import rx.functions.Action1;

@Service(value = "socketIOService")
public class SocketIOServiceImpl implements SocketIOService {

    // 用来存已连接的客户端
    private static Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>();

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
            if (loginUserNum != null) {
                clientMap.put(loginUserNum, client);
            }
        });

        // 监听客户端断开连接
        socketIOServer.addDisconnectListener(client -> {
            String loginUserNum = getParamsByClient(client);
            if (loginUserNum != null) {
                clientMap.remove(loginUserNum);
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
    public Boolean pushMessageToUser(String clientId, PushMessage pushMessage) {
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        // 处理自定义的事件，与连接监听类似
        socketIOServer.addEventListener(RESPONSE_EVENT, String.class, (subClient, data, ackSender) -> {
            isSuccess.set(true);
        });

        SocketIOClient client = clientMap.get(clientId);
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
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Boolean> future = executor.submit(callable);

        try {
            Boolean result = future.get();
            executor.shutdown();

            socketIOServer.removeAllListeners(RESPONSE_EVENT);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
