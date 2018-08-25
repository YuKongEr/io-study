package com.yukong.nio;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ClientHandler implements Runnable {

    private Selector selector;

    public ClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if(selector.select(1000) < 0) {
                 continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = keys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey sc = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    //读消息
                    if (sc.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) sc.channel();
                        //创建ByteBuffer，并开辟一个1M的缓冲区

                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        //读取请求码流，返回读取到的字节数
                        int byteSize = socketChannel.read(byteBuffer);
                        if (byteSize > 0) {
                            //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                            byteBuffer.flip();
                            //根据缓冲区可读字节数创建字节数组 总长度减去空余的
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            // 复制至新的缓冲字节流
                            byteBuffer.get(bytes);
                            String message = new String(bytes, "UTF-8");
                            System.out.println(message);
                        }
                    }
                }
            }
        } catch (IOException e) {

        }
    }
}
