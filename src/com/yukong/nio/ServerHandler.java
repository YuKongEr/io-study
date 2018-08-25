package com.yukong.nio;

import com.yukong.util.ResponseUtil;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 *  服务端业务逻辑处理器
 */
public class ServerHandler implements Runnable{

    private Selector selector;

    public ServerHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
        while (true) {
            // 等待某信道就绪(或超时)
            if(selector.select(1000)==0){
//                System.out.print("独自等待.");
                continue;
            }
            // 取得迭代器.selectedKeys()中包含了每个准备好某一I/O操作的信道的SelectionKey
            Iterator<SelectionKey> keyIterator=selector.selectedKeys().iterator();
           while (keyIterator.hasNext()){
               SelectionKey sk = keyIterator.next();
               // 删除已选的key 以防重负处理
               keyIterator.remove();
               // 处理key
               handlerSelect(sk);

            }

        }


       } catch (IOException e) {

       }
    }

    private void handlerSelect(SelectionKey sk) throws IOException {
        // 处理新接入的请求
        if (sk.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) sk.channel();
            //通过ServerSocketChannel的accept创建SocketChannel实例
            //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
            SocketChannel sc = ssc.accept();
            //设置为非阻塞的
            sc.configureBlocking(false);
            //注册为读
            sc.register(selector, SelectionKey.OP_READ);
        }
        // 读操作
        if (sk.isReadable()) {
            String request, response;
            SocketChannel sc = (SocketChannel) sk.channel();
            // 创建一个ByteBuffer 并设置大小为1m
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            // 获取到读取的字节长度
            int readBytes = sc.read(byteBuffer);
            // 判断是否有数据
            if (readBytes > 0) {
                //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                byteBuffer.flip();
                //根据缓冲区可读字节数创建字节数组
                byte[] bytes = new byte[byteBuffer.remaining()];
                // 复制至新的缓冲字节流
                byteBuffer.get(bytes);
                request = new String(bytes, "UTF-8");
                System.out.println("[" + Thread.currentThread().getName()+ "]" + "小yu机器人收到消息：" + request);
                // 具体业务逻辑处理 查询信息。
                response = ResponseUtil.queryMessage(request);
                //将消息编码为字节数组
                byte[] responseBytes = response.getBytes();
                //根据数组容量创建ByteBuffer
                ByteBuffer writeBuffer = ByteBuffer.allocate(responseBytes.length);
                //将字节数组复制到缓冲区
                writeBuffer.put(responseBytes);
                //flip操作
                writeBuffer.flip();
                //发送缓冲区的字节数组
                sc.write(writeBuffer);
            }
        }
    }


}
