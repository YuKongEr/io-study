package com.yukong.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 * 服务端
 */
public class Server {

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6789;

    public  void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public  void start(Integer port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("小yu机器人启动，监听端口为：" + port);
        //通过无线循环监听客户端连接
        while (true) {
            // 阻塞方法，直至有客户端连接成功
            Socket socket = serverSocket.accept();
            // 多线程处理客户端请求
            new Thread(new ServerHandler(socket)).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }


}
