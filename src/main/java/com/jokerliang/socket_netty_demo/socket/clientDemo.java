package com.jokerliang.socket_netty_demo.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class clientDemo {
    public static void main(String[] args) throws IOException {
        int serverPort = 8768;
        Socket socket = new Socket("192.168.31.57", serverPort);
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        outputStream.write("中文2".getBytes("UTF-8"));
        outputStream.flush();
        byte[] byteArray = new byte[1024];
        int length = inputStream.read(byteArray);
        String response = new String(byteArray, 0, length, "UTF-8");
        System.out.println(response);

        inputStream.close();
        outputStream.close();
        socket.close();
        outputStream.close();
        socket.close();
    }
}
