package com.yukong.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * 客户端
 */
public class Client {


    // 通道选择器
    private Selector selector;

    // 与服务器通信的通道
    SocketChannel socketChannel;

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6780;

    /**
     * 默认端口
     */
    private static final String DEFAULT_HOST = "127.0.0.1";

    public  void send(String key) throws IOException {
        send(DEFAULT_PORT, DEFAULT_HOST, key);
    }
    public  void send(int port,String host, String key) throws IOException {
        init(port, host);
        System.out.println("查询的key为：" + key);
        //将消息编码为字节数组
        byte[] bytes = key.getBytes();
        //根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        //将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        //flip操作
        writeBuffer.flip();
        //发送缓冲区的字节数组
        socketChannel.write(writeBuffer);
        //****此处不含处理“写半包”的代码
    }

    public void init(int port,String host) throws IOException {
        // 创建选择器
        selector = Selector.open();

        // 设置链接的服务端地址
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        // 打开通道
        socketChannel = SocketChannel.open(socketAddress);// 非阻塞
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new ClientHandler(selector)).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client();
        while (scanner.hasNext()) {
            String key = scanner.next();
            client.send(key);
        }
    }

}
