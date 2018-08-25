package com.yukong.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 * 客户端
 */
public class Client {

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6789;

    /**
     * 默认端口
     */
    private static final String DEFAULT_HOST = "localhost";

    public  void send(String key){
        send(DEFAULT_PORT,key);
    }
    public  void send(int port,String key){
        System.out.println("查询的key为：" + key);
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try{
            socket = new Socket(DEFAULT_HOST,port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            out.println(key);
            System.out.println("查询的结果为：" + in.readLine());
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            if(out != null){
                out.close();
                out = null;
            }
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client();
        while (scanner.hasNext()) {
            String key = scanner.next();
            client.send(key);
        }
    }

}
