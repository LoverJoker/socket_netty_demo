package com.jokerliang.socket_netty_demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-28 09:54
 */
public class ThreadUtils {

    private static  ThreadUtils instance;

    public static synchronized ThreadUtils getInstance() {
        if (instance == null) {
            instance = new ThreadUtils();
        }
        return instance;
    }

    private ExecutorService executor = Executors.newCachedThreadPool();


    public ExecutorService getExecutorService() {
        return executor;
    }
}
