package com.yukong.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * 服务端
 */
public class Server {


    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6780;

    public  void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public  void start(Integer port) throws IOException {
        // 打开多路复用选择器
        Selector selector = Selector.open();
        //  打开服务端监听通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 绑定监听的端口
        serverSocketChannel.bind(new InetSocketAddress(port));
        // 将选择器绑定到监听信道,只有非阻塞信道才可以注册选择器.并在注册过程中指出该信道可以进行Accept操作
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("小yu机器人启动，监听端口为：" + port);
        new Thread(new ServerHandler(selector)).start();
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

}
