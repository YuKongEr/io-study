package com.yukong.aio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ExecutionException;

public class Client {

    class ClientCompletionHandler implements CompletionHandler<Void, Void> {

        private AsynchronousSocketChannel channel;
        private CharBuffer charBufferr = null;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
        private BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

        public ClientCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }


        @Override
        public void completed(Void result, Void attachment) {

            System.out.println("Input Client Reuest:");
            String request;
            try {
                request = clientInput.readLine();
                channel.write(ByteBuffer.wrap(request.getBytes()));
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                while(channel.read(buffer).get() != -1){
                    buffer.flip();
                    charBufferr = decoder.decode(buffer);
                    System.out.println(charBufferr.toString());
                    if(buffer.hasRemaining()){
                        buffer.compact();
                    }
                    else{
                        buffer.clear();
                    }
                    request = clientInput.readLine();
                    channel.write(ByteBuffer.wrap(request.getBytes())).get();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            throw  new  RuntimeException("channel not opened!");
        }

    }

    public void start() throws IOException, InterruptedException{
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        if(channel.isOpen()){
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 128*1024);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 128*1024);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE,true);
            channel.connect(new InetSocketAddress("127.0.0.1",6780),null,new ClientCompletionHandler(channel));
            while(true){
                Thread.sleep(5000);
            }
        }
        else{
            throw new RuntimeException("Channel not opened!");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Client client = new Client();
        client.start();
    }


}
