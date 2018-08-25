package com.yukong.aio;

import com.yukong.util.ResponseUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ExecutionException;

/**
 *  异步非阻塞服务端
 */
public class Sever {

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6780;

    private AsynchronousServerSocketChannel serverChannel;


    //作为handler接收客户端连接
    class ServerCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

        private AsynchronousServerSocketChannel serverChannel;
        private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        private CharBuffer charBuffer;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

        public ServerCompletionHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }


        @Override
        public void completed(AsynchronousSocketChannel result, Void attachment) {

            //立即接收下一个请求,不停顿
            serverChannel.accept(null, this);
            try {
                while (result.read(buffer).get() != -1) {
                    buffer.flip();
                    charBuffer = decoder.decode(buffer);
                    String request = charBuffer.toString().trim();
                    System.out.println("[" + Thread.currentThread().getName()+ "]" + "小yu机器人收到消息：" + request);
                    // 具体业务逻辑处理 查询信息。
                    String response = ResponseUtil.queryMessage(request);
                    //将消息编码为字节数组
                    byte[] responseBytes = response.getBytes();
                    //根据数组容量创建ByteBuffer
                    ByteBuffer outBuffer = ByteBuffer.allocate(responseBytes.length);
                    //将字节数组复制到缓冲区
                    outBuffer.put(responseBytes);
                    //flip操作
                    outBuffer.flip();
                    //发送缓冲区的字节数组
                    result.write(outBuffer).get();
                    if (buffer.hasRemaining()) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            } finally {
                try {
                    result.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            //立即接收下一个请求,不停顿
            serverChannel.accept(null, this);
            throw new RuntimeException("connection failed!");
        }

    }


    public void init() throws IOException, InterruptedException {
        init(DEFAULT_PORT);
    }

    public void init(Integer port) throws IOException, InterruptedException {
        // 打开异步通道
        this.serverChannel = AsynchronousServerSocketChannel.open();
        // 判断通道是否打开
        if (serverChannel.isOpen()) {
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(port));
        } else {
            throw new RuntimeException("Channel not opened!");
        }
        start(port);
    }

    public void start(Integer port) throws InterruptedException {
        System.out.println("小yu机器人启动，监听端口为：" + port);
        this.serverChannel.accept(null, new ServerCompletionHandler(serverChannel));
        // 保证线程不会挂了
        while (true) {
            Thread.sleep(5000);
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Sever server = new Sever();
        server.init();
    }
}
