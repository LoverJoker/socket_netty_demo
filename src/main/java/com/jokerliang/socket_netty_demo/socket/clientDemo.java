package com.jokerliang.socket_netty_demo.socket;

import com.jokerliang.socket_netty_demo.device.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class clientDemo {
    public static void main(String[] args) throws IOException {
        int serverPort = 8768;
        Socket socket = new Socket("localhost", serverPort);
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        outputStream.write("中文2".getBytes("UTF-8"));
        outputStream.flush();
        byte[] byteArray = new byte[6];
        int length = inputStream.read(byteArray);
        String s = ByteUtils.byteArrayToHexString(byteArray);
        String response = new String(byteArray, 0, length, "UTF-8");
        System.out.println(s);

        inputStream.close();
        outputStream.close();
        socket.close();
        outputStream.close();
        socket.close();
    }
}
