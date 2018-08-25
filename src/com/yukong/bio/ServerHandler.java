package com.yukong.bio;

import com.yukong.util.ResponseUtil;

import java.io.*;
import java.net.Socket;

/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 *  服务端业务逻辑处理器
 */
public class ServerHandler implements Runnable{

    private Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // 获取socket的字符缓存输入流 也就是获取客户端给服务器的字符流
            in = new BufferedReader( new InputStreamReader(this.socket.getInputStream()));
            // 获取socket的字符输出流 也就是发送的客户的字符流 第二个参数自动刷新
            out = new PrintWriter( new OutputStreamWriter(this.socket.getOutputStream()), true);
            String request, response;
            // 读取输入流的消息 如果为空 则退出读取
            while ((request = in.readLine()) != null) {
                System.out.println("[" + Thread.currentThread().getName()+ "]" + "小yu机器人收到消息：" + request);
                // 具体业务逻辑处理 查询信息。
                response = ResponseUtil.queryMessage(request);
                out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            // 资源释放
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }


}
