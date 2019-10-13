package com.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DbHandler {
    public String ID;  // 模具ID
    private String IP;  // 模具上传时IP
    private String ipStatus;    // 模具上传时IP相较一天前是否改变
    public Queue<List<String>> dataQue;    // 存储处理数据的队列

    private Connection conn;    // 数据库连接
    private DbHandler db;   // 私有化对象

    private String lastData = "";   // 上次上传的第一条数据

    /**
     * 带参构造函数，接收到来自模具的数据时初始化类
     *
     * @param ID 上传数据的模具ID
     * @param IP 模具上传时的IP地址
     */
    public DbHandler(String ID, String IP, Queue<List<String>> dataQue) {
        this.ID = ID;
        this.IP = IP;
        this.dataQue = dataQue;
    }

    /**
     * 处理数据，拆分为List后再存入队列
     *
     * @param info 上传的数据，从缓冲区取出
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
         * 将处理完后的List添加到队列
         */
        dataQue.add(data);
    }

    /**
     * @param conn 数据库连接
     * @return IP地址在一天内是否变化
     */
    public boolean hasIpChanged(Connection conn) {
        String sql = "select IP from `" + ID + "` order by Record desc limit 1";
//        String sql1 = "select IP from `" + ID + "` where date(DtTm) = " +
//                "date_sub(curdate(),interval 1 day) and MD_ID = '" + ID + "' limit 1;";
        String[] pre_IP = null;
        PreparedStatement pst = null;
        ResultSet res = null;
        try {
            pst = conn.prepareStatement(sql);  //预处理语句
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
     * 实际执行数据插入的函数
     *
     * @param conn 数据库连接
     */
    public void excuteInsert(Connection conn) {
        ipStatus = "Unchange"; // 标志当前模具上传IP地址相比一天前是否变化

        /**
         * IP状态
         */
        if (hasIpChanged(conn)) {
            ipStatus = "Changed";
        }

        /**
         * 根据模具ID将接收队列中的数据写入
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
            PreparedStatement pst = conn.prepareStatement(sql);  //预处理语句
            pst.executeUpdate();

            pst.close();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    /**
     * dataQue中数据大于四条时插入数据库
     * 先判断数据库中是否存在该模具表，若不存在则创建
     */
    public void insert() {

        try{
            conn = DbUtils.getConnection(); // 从连接池得到数据库连接
            conn.setAutoCommit(false);  // 关闭数据库自动提交
        }catch (SQLException e){
            e.printStackTrace();
        }

        /**
         * 若表不存在则以模具ID创建表
         */
        String createTabelSql = "CREATE TABLE IF NOT EXISTS `" + ID + "`(\n" +
                "\tRecord int auto_increment primary key comment '主键自增',\n" +
                "\tMD_ID varchar(16) comment '设备序列号',  \n" +
                "\tFil int comment '文件名',\n" +
                "\tDtTm datetime comment '日期时间',\n" +
                "\tTotl int comment '成型总数',\n" +
                "\tMdAv float(6,1) comment '成型平均时间（单位：秒）',\n" +
                "\tLstAv float(6,1) comment '上批次成型平均时间（单位：秒）',\n" +
                "\tMdEf float(4,2) comment '效率百分比（0.95 ->95%）',\n" +
                "\tLstEf float(4,2) comment '上批次效率百分比（0.95 ->95%）',\n" +
                "\tCurPer int comment '当前周期数',\n" +
                "\tMdTm int comment '模具检修次数',\n" +
                "\tDis varchar(16) comment '拆除状态',\n" +
                "\tIP varchar(16) comment '数据上传时的WiFi',\n" +
                "\tApChange varchar(16) comment 'Wifi接入点是否发生变化',\n" +
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
        excuteInsert(conn); // 每条执行插入

        /**
         * 每四条重复执行数据插入
         */
//        System.out.println("数据队列长度：" + dataQue.size());
//        while (dataQue.size() >= 4) {
//            excuteInsert(conn);
//        }
        /**
         * 释放连接
         */
        releaseConn();
    }

    /**
     * 回收数据库连接，释放至连接池
     */
    public void releaseConn() {
        DbUtils.close(conn);
//        System.out.println("已放回连接池");
    }
}