package com.server;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DbUtils {
    static ComboPooledDataSource cpds = null;
    static {
        cpds = new ComboPooledDataSource("mysql");
    }

    /**
     * 获得数据库连接
     * @return  Connection
     */
    public static Connection getConnection(){
        try{
            return cpds.getConnection();
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public static void close(Connection conn){
        /**
         * 关闭数据库连接
         */
        if(conn != null){
            try {
                conn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
}