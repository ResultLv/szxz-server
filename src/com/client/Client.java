package com.client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ClientSocket implements Runnable{
//    String IP = "112.74.50.44";
//    String IP = "47.106.148.132";
    String IP = "localhost";
    @Override
    public void run() {
        try{
            Socket socket = new Socket(IP, 3333);
            OutputStream outputStream=socket.getOutputStream();//��ȡһ��������������˷�����Ϣ
            PrintStream printStream=new PrintStream(outputStream);//���������װ�ɴ�ӡ��
            InputStream inputStream=socket.getInputStream();//��ȡһ�������������շ���˵���Ϣ
            InputStreamReader inputStreamReader=new InputStreamReader(inputStream);//��װ���ַ��������Ч��
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);//������

            List<String> info = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                info.add("ID=XXOOXX23330:");
                info.add("Fil:0000551;DtTm:2018-11-06 16:52:41;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000005;MdTm:00000;Dis:0#");
                info.add("Fil:0000551;DtTm:2018-11-07 16:53:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000006;MdTm:00000;Dis:1#");
                info.add("Fil:0000551;DtTm:2018-11-08 16:54:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000007;MdTm:00000;Dis:1#");
                info.add("Fil:0000551;DtTm:2018-11-09 16:55:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000008;MdTm:00000;Dis:1#");

                info.add("ID=XXOOXX23330:");
                info.add("Get-Real-Date-Time.");
//                for(int j = 0; j < 4; j++){
//                    info.add("ID=XXOOXX23330:");
//                    info.add("Fil:0000551;DtTm:2018-11-06 16:52:41;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000005;MdTm:00000;Dis:0#");
//                    info.add("Fil:0000551;DtTm:2018-11-07 16:53:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000006;MdTm:00000;Dis:1#");
//                    info.add("Fil:0000551;DtTm:2018-11-08 16:54:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000007;MdTm:00000;Dis:1#");
//                    info.add("Fil:0000551;DtTm:2018-11-09 16:55:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000008;MdTm:00000;Dis:1#");
//
//                    info.add("Fil:0000552;DtTm:2018-11-06 16:52:41;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000005;MdTm:00000;Dis:0#");
//                    info.add("Fil:0000552;DtTm:2018-11-07 16:53:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000006;MdTm:00000;Dis:1#");
//                    info.add("Fil:0000552;DtTm:2018-11-08 16:54:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000007;MdTm:00000;Dis:1#");
//                    info.add("Fil:0000552;DtTm:2018-11-09 16:55:42;Totl:0000005;MdAv:1234;LstAv:9999;MdEf:126%;LstEf:126%;CurPer:0000008;MdTm:00000;Dis:1#");
//                }
                System.out.println(i+1);
            }

            int k = 0;
            while(k < info.size()){
                printStream.println(info.get(k));
                k++;
            }

//            String temp;
//            while((temp = bufferedReader.readLine()) != null){
//                System.out.println(temp);
//            }
                //�ر����Ӧ����Դ
            bufferedReader.close();
            inputStream.close();
            printStream.close();
            outputStream.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

/**
 * Socket�ͻ���
 */
public class Client{

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

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            Thread thread = new Thread(new ClientSocket());
            thread.start();
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

//        System.out.println((byte)Integer.parseInt("2", 16));
//        System.out.println(byteToHex((byte)Integer.parseInt("2", 16)));
    }
}