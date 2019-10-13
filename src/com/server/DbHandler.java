package com.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DbHandler {
    public String ID;  // ģ��ID
    private String IP;  // ģ���ϴ�ʱIP
    private String ipStatus;    // ģ���ϴ�ʱIP���һ��ǰ�Ƿ�ı�
    public Queue<List<String>> dataQue;    // �洢�������ݵĶ���

    private Connection conn;    // ���ݿ�����
    private DbHandler db;   // ˽�л�����

    private String lastData = "";   // �ϴ��ϴ��ĵ�һ������

    /**
     * ���ι��캯�������յ�����ģ�ߵ�����ʱ��ʼ����
     *
     * @param ID �ϴ����ݵ�ģ��ID
     * @param IP ģ���ϴ�ʱ��IP��ַ
     */
    public DbHandler(String ID, String IP, Queue<List<String>> dataQue) {
        this.ID = ID;
        this.IP = IP;
        this.dataQue = dataQue;
    }

    /**
     * �������ݣ����ΪList���ٴ������
     *
     * @param info �ϴ������ݣ��ӻ�����ȡ��
     */
    public void addData(String info) {
        List<String> data = new ArrayList<>();
        String[] tmp = null;
        try {
            tmp = info.split(";");
            for (int i = 0; i < tmp.length; i++) {
                int index = tmp[i].indexOf(':');
                tmp[i] = tmp[i].substring(index + 1);
            }
            for (int i = 0; i < tmp.length; i++) {
                if (i == 0 || i == 2 || i == 7 || i == 8)
                    tmp[i] = String.valueOf(Integer.valueOf(tmp[i]));
                if (i == 3 || i == 4) {
                    if (tmp[i].contains(":")) {
                        String[] temp = tmp[i].split(":");
                        int temp1 = Integer.valueOf(temp[0]);
                        int temp2 = Integer.valueOf(temp[1]);
                        tmp[i] = String.valueOf(temp1 * 60 + temp2);
                    } else {
                        tmp[i] = String.valueOf(Float.valueOf(tmp[i]));
                    }
                }
                if (i == 5 || i == 6) {
                    float temp = Integer.valueOf(tmp[i].substring(0, tmp[i].length() - 1)) / 100.0f;
                    tmp[i] = String.valueOf(temp);
                }
                if (i == 9) {
                    int temp = Integer.valueOf(tmp[i].substring(0, 1));
                    tmp[i] = temp == 1 ? "Unremove" : "Removed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < tmp.length; i++) {
            data.add(tmp[i]);
        }
        /**
         * ����������List��ӵ�����
         */
        dataQue.add(data);
    }

    /**
     * @param conn ���ݿ�����
     * @return IP��ַ��һ�����Ƿ�仯
     */
    public boolean hasIpChanged(Connection conn) {
        String sql = "select IP from `" + ID + "` order by Record desc limit 1";
//        String sql1 = "select IP from `" + ID + "` where date(DtTm) = " +
//                "date_sub(curdate(),interval 1 day) and MD_ID = '" + ID + "' limit 1;";
        String[] pre_IP = null;
        PreparedStatement pst = null;
        ResultSet res = null;
        try {
            pst = conn.prepareStatement(sql);  //Ԥ�������
            res = pst.executeQuery();

            if (res.next()) {
                pre_IP = res.getString(1).split("\\.");
            } else {
                pst.close();
                res.close();
                return false;
            }
            pst.close();
            res.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] now_IP = IP.split("\\.");
        if (!now_IP[0].equals(pre_IP[0]) || !now_IP[1].equals(pre_IP[1]) || !now_IP[2].equals(pre_IP[2])) {
            return true;
        }
        return false;
    }


    /**
     * ʵ��ִ�����ݲ���ĺ���
     *
     * @param conn ���ݿ�����
     */
    public void excuteInsert(Connection conn) {
        ipStatus = "Unchange"; // ��־��ǰģ���ϴ�IP��ַ���һ��ǰ�Ƿ�仯

        /**
         * IP״̬
         */
        if (hasIpChanged(conn)) {
            ipStatus = "Changed";
        }

        /**
         * ����ģ��ID�����ն����е�����д��
         */
        String prefix = "INSERT INTO `" + ID + "` (MD_ID,Fil,DtTm,Totl,MdAv," +
                "LstAv,MdEf,LstEf,CurPer,MdTm,Dis,IP,APChange) SELECT ";
        List<String> data = dataQue.poll();

        String infix = "";
        infix = "'" + ID + "','" + data.get(0) + "','" + data.get(1) + "','" + data.get(2) + "','"
                + data.get(3) + "','" + data.get(4) + "','" + data.get(5) + "','" + data.get(6) + "','" + data.get(7)
                + "','" + data.get(8) + "','" + data.get(9) + "','" + IP + "','" + ipStatus + "'";

        String suffix = "FROM DUAL WHERE NOT EXISTS(SELECT DtTm FROM `" + ID + "` WHERE DtTm = '" + data.get(1) + "')";

        String sql = prefix + infix + suffix;

//        String prefix = "INSERT INTO `" + ID + "` (MD_ID,Fil,DtTm,Totl,MdAv," +
//                "LstAv,MdEf,LstEf,CurPer,MdTm,Dis,IP,APChange) VALUES ";
//        String surfix = "";
//
//        List<String> data = dataQue.poll();
//
//
//        surfix = "\n('" + ID + "','" + data.get(0) + "','" + data.get(1) + "','" + data.get(2) + "','"
//                + data.get(3) + "','" + data.get(4) + "','" + data.get(5) + "','" + data.get(6) + "','" + data.get(7)
//                + "','" + data.get(8) + "','" + data.get(9) + "','" + IP + "','" + ipStatus + "')";
//
//        String sql = prefix + surfix;

//        System.out.println(sql);
        try {
            PreparedStatement pst = conn.prepareStatement(sql);  //Ԥ�������
            pst.executeUpdate();

            pst.close();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    /**
     * dataQue�����ݴ�������ʱ�������ݿ�
     * ���ж����ݿ����Ƿ���ڸ�ģ�߱����������򴴽�
     */
    public void insert() {

        try{
            conn = DbUtils.getConnection(); // �����ӳصõ����ݿ�����
            conn.setAutoCommit(false);  // �ر����ݿ��Զ��ύ
        }catch (SQLException e){
            e.printStackTrace();
        }

        /**
         * ������������ģ��ID������
         */
        String createTabelSql = "CREATE TABLE IF NOT EXISTS `" + ID + "`(\n" +
                "\tRecord int auto_increment primary key comment '��������',\n" +
                "\tMD_ID varchar(16) comment '�豸���к�',  \n" +
                "\tFil int comment '�ļ���',\n" +
                "\tDtTm datetime comment '����ʱ��',\n" +
                "\tTotl int comment '��������',\n" +
                "\tMdAv float(6,1) comment '����ƽ��ʱ�䣨��λ���룩',\n" +
                "\tLstAv float(6,1) comment '�����γ���ƽ��ʱ�䣨��λ���룩',\n" +
                "\tMdEf float(4,2) comment 'Ч�ʰٷֱȣ�0.95 ->95%��',\n" +
                "\tLstEf float(4,2) comment '������Ч�ʰٷֱȣ�0.95 ->95%��',\n" +
                "\tCurPer int comment '��ǰ������',\n" +
                "\tMdTm int comment 'ģ�߼��޴���',\n" +
                "\tDis varchar(16) comment '���״̬',\n" +
                "\tIP varchar(16) comment '�����ϴ�ʱ��WiFi',\n" +
                "\tApChange varchar(16) comment 'Wifi������Ƿ����仯',\n" +
                "\tindex Date_index(DtTm),\n" +
                "\tindex IpChange_index(ApChange),\n" +
                "\tindex Dis_index(Dis)" +
                ");";
        try {
            PreparedStatement pst = conn.prepareStatement(createTabelSql);
            pst.executeUpdate();
            pst.close();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        excuteInsert(conn); // ÿ��ִ�в���

        /**
         * ÿ�����ظ�ִ�����ݲ���
         */
//        System.out.println("���ݶ��г��ȣ�" + dataQue.size());
//        while (dataQue.size() >= 4) {
//            excuteInsert(conn);
//        }
        /**
         * �ͷ�����
         */
        releaseConn();
    }

    /**
     * �������ݿ����ӣ��ͷ������ӳ�
     */
    public void releaseConn() {
        DbUtils.close(conn);
//        System.out.println("�ѷŻ����ӳ�");
    }
}