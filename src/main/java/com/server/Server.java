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
     * �ӡ�ID=147436583431:���л�ȡģ��ID
     * @param info ���յ�һ������
     * @return  ģ��ID
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
     * Уʱ����
     * �յ��ϴ���Get-Real-Date-Time.�ֶκ󷵻ط�����ʱ��
     * @return ��������ǰʱ��
     */
    public static byte[] timming(){
        byte[] res = new byte[25];
        String time = "Set-Real-Date-Time=";
        SimpleDateFormat df = new SimpleDateFormat("yy MM dd HH mm ss");
        String temp = df.format(new Date());
        String[] tmp = temp.split(" ");
        temp = "";

        // "Set-Real-Date-Time="תΪbyte����
        byte[] sb = time.getBytes();
        for (int i = 0; i < sb.length; i++) {
            res[i] = sb[i];
        }

        // Date�ַ���תΪbyte����
        for (int i = 0; i < tmp.length; i++) {
            res[i+19] =  (byte)Integer.parseInt(tmp[i], 16);
        }

        for(int i = 0; i < res.length; i++){
            temp += byteToHex(res[i]) + " ";
        }
        System.out.println(temp);

        return res; // ����byte����
    }

    /**
     * �ֽ�תʮ������
     * @param b ��Ҫ����ת����byte�ֽ�
     * @return  ת�����Hex�ַ���
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
            System.out.println("Socket�߳�: " + Thread.currentThread().getName());
            System.out.println("Received from " + IP + " : " + socket.getLocalPort());

            String ID = "";
            Queue<List<String>> dataQue = new LinkedList<>();   // �������ݶ���ʵ��
            db = new DbHandler(ID, IP, dataQue);   // ����DbHandlerʵ��
            while((info = bufferedReader.readLine()) != null) {
                System.out.println(info);
                /** 1.�ж��Ƿ�ΪУʱ������򷵻ط�������ǰʱ��
                 *  2.�ж��Ƿ����ID,�����ȡID
                 *  3.����������Ϊ���������򴴽�DbHandlerʵ�����д���
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
                 * ������ݶ��д���4����֤�����յ�����������
                 * Ȼ��ִ�в������
                 */
//                if(db.dataQue.size() >= 4){
//                    db.insert();
//                }
            }
            // �ر�IO������
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
        DbUtils.getConnection();// �������ӳ�

        ExecutorService cachedThreadPoll = Executors.newCachedThreadPool(); //�Զ���С�̳߳�

        /**
         * ����ض��򣬱�����Ϊlog�ļ�
         */
        try{
            PrintStream newStream = new PrintStream("./log.txt"); // �����µ������
            System.setOut(newStream); // �����µ������,��Ϊ��������ݸ��ĵ��µ����������
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        try {
            final ServerSocket serverSocket = new ServerSocket(3333);
            System.out.println("�ȴ�������...");

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