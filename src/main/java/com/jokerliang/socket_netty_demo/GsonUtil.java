package com.jokerliang.socket_netty_demo;

import com.google.gson.Gson;

/**
 * @author jokerLiang
 */
public class GsonUtil {

    private static Gson gson = null;

    private GsonUtil() {
    }

    public synchronized static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

}
