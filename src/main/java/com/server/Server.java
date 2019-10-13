/**
 *  author: ResutltLv
 *  encoding: GBK
 */
package com.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SocketThread extends Thread{

    private Socket socket;

    public SocketThread(Socket socket) {
        this.socket = socket;
    }

    /**
     * 从“ID=147436583431:”中获取模具ID
     * @param info 接收的一条数据
     * @return  模具ID
     */
    public String getID(String info){
        String ID = null;
        if(info.contains("ID")){
            String temp = info.split("=")[1];
            ID = temp.substring(0, temp.length()-1);
        }
        return ID;
    }

    /**
     * 校时函数
     * 收到上传的Get-Real-Date-Time.字段后返回服务器时间
     * @return 服务器当前时间
     */
    public static byte[] timming(){
        byte[] res = new byte[25];
        String time = "Set-Real-Date-Time=";
        SimpleDateFormat df = new SimpleDateFormat("yy MM dd HH mm ss");
        String temp = df.format(new Date());
        String[] tmp = temp.split(" ");
        temp = "";

        // "Set-Real-Date-Time="转为byte数组
        byte[] sb = time.getBytes();
        for (int i = 0; i < sb.length; i++) {
            res[i] = sb[i];
        }

        // Date字符串转为byte数组
        for (int i = 0; i < tmp.length; i++) {
            res[i+19] =  (byte)Integer.parseInt(tmp[i], 16);
        }

        for(int i = 0; i < res.length; i++){
            temp += byteToHex(res[i]) + " ";
        }
        System.out.println(temp);

        return res; // 返回byte数组
    }

    /**
     * 字节转十六进制
     * @param b 需要进行转换的byte字节
     * @return  转换后的Hex字符串
     */
    public static String byteToHex(byte b){
        String hex = Integer.toHexString(b & 0xFF);
        if(hex.length() < 2){
            hex = "0" + hex;
        }
        return hex;
    }

    public void run() {
        System.out.println("-----------------------------");
        DbHandler db;
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            String info;
            String IP = socket.getInetAddress().getHostAddress();
            System.out.println("Socket线程: " + Thread.currentThread().getName());
            System.out.println("Received from " + IP + " : " + socket.getLocalPort());

            String ID = "";
            Queue<List<String>> dataQue = new LinkedList<>();   // 创建数据队列实例
            db = new DbHandler(ID, IP, dataQue);   // 创建DbHandler实例
            while((info = bufferedReader.readLine()) != null) {
                System.out.println(info);
                /** 1.判断是否为校时命令，是则返回服务器当前时间
                 *  2.判断是否包含ID,是则获取ID
                 *  3.若都不是且为生产数据则创建DbHandler实例进行处理
                 */
                if(info.contains("Get")){
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
//                    printStream.println("xxx");
                    outputStream.write(timming());
                }else if(info.contains("ID")){
                    ID = getID(info);
                    if(db.ID != ID)
                        db.ID = ID;
                }else if(info.contains("Fil")) {
                    db.addData(info);
                    db.insert();
                }

                /**
                 * 如果数据队列大于4，则证明接收到了生产数据
                 * 然后执行插入操作
                 */
//                if(db.dataQue.size() >= 4){
//                    db.insert();
//                }
            }
            // 关闭IO流连接
            printStream.close();
            bufferedReader.close();
            inputStream.close();
            outputStream.close();
            socket.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    /**
     * Socket Server
     */
    public static void main(String[] args) {
        DbUtils.getConnection();// 启动连接池

        ExecutorService cachedThreadPoll = Executors.newCachedThreadPool(); //自动大小线程池

        /**
         * 输出重定向，保存作为log文件
         */
        try{
            PrintStream newStream = new PrintStream("./log.txt"); // 创建新的输出流
            System.setOut(newStream); // 设置新的输出流,即为将输出内容更改到新的输出对象中
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        try {
            final ServerSocket serverSocket = new ServerSocket(3333);
            System.out.println("等待连接中...");

            while(true) {
                Socket socket = serverSocket.accept();
                SocketThread socketThread = new SocketThread(socket);
                cachedThreadPoll.execute(socketThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}