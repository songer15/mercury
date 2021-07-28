package com.valor.mercury.executor.test;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;

public class HiveTest {
    private static String tableName = "testHiveDriverTable";
    private Connection connection;
    private Statement stmt;

    @Before
    public void connectionReady() throws Exception{
         connection = DriverManager.getConnection("jdbc:hive2://51.81.244.223:10000/default;auth=noSasl");
         stmt = connection.createStatement();
    }

    @Test
    public void createTable() throws Exception {

        stmt.execute("drop table if exists " + tableName);
        stmt.execute("create table " + tableName + " (key int, value string)");
        // show tables
        String sql = "show tables '" + tableName + "'";
        System.out.println("Running: " + sql);
        ResultSet res = stmt.executeQuery(sql);
        if (res.next()) {
            System.out.println(res.getString(1));
        }

    }

    @Test
    public void describeTable() throws Exception {
        // describe table
        String sql = "describe " + tableName;
        System.out.println("Running: " + sql);
        ResultSet res = stmt.executeQuery(sql);
        while (res.next()) {
            System.out.println(res.getString(1) + "\t" + res.getString(2));
        }
    }

    @Test
    public void loadTable() throws Exception {
        // describe table
                 // load data into table
                // NOTE: filepath has to be local to the hive server
                // NOTE: /tmp/a.txt is a ctrl-A separated file with two fields per line
                //String filepath = "/tmp/a.txt";
        String  sql = "load data inpath 'hdfs://mycluster/test/load1' into table " + tableName;
        System.out.println("Running: " + sql);
        stmt.execute(sql);
    }

    @Test
    public void selectTable() throws Exception {
        connectionReady();
        Connection con = DriverManager.getConnection("jdbc:hive2://51.81.244.223:10000/default;auth=noSasl");
        Statement stmt = con.createStatement();
        // describe table
        String sql = "select * from " + tableName;
        System.out.println("Running: " + sql);
        ResultSet res = stmt.executeQuery(sql);
        while (res.next()) {
            System.out.println(String.valueOf(res.getInt(1)) + "\t" + res.getString(2));
        }
    }

    @Test
    public void insertIntoSelect() throws Exception {
        // describe table
        connectionReady();
        String sql = "insert into  " + tableName + " values (1, '2')";
        System.out.println("Running: " + sql);
        stmt.execute(sql);
//        while (res.next()) {
//            System.out.println(String.valueOf(res.getInt(1)) + "\t" + res.getString(2));
//        }
    }


    @Test
    public void sqlSplit() {
        String SQL = " LOAD DATA inpath '/mfc/activation/recharge_card_info/*-parameter1' overwrite INTO TABLE mfc.recharge_card_info;\n" +
                "\n" +
                "INSERT INTO report_forms.mfc_activation_recharge_card\n" +
                "SELECT concat(a.times,a.biz_value) AS cid,\n" +
                "       'recharge_card' AS actionType,\n" +
                "       a.times AS actionTime,\n" +
                "       unix_timestamp()*1000 AS hiveTime,\n" +
                "                            COUNT(1) AS totalCount,\n" +
                "                            a.biz_value AS bizValue\n" +
                "FROM\n" +
                "  (SELECT biz_value,\n" +
                "          from_unixtime(floor(used_ts/1000),'yyyy-MM-dd') AS times\n" +
                "   FROM mfc.recharge_card_info\n" +
                "   WHERE used_ts BETWEEN parameter2 AND parameter3) a\n" +
                "GROUP BY a.times,\n" +
                "         a.biz_value;\n" +
                "\n" +
                "\n" +
                "SELECT *\n" +
                "FROM report_forms.mfc_activation_recharge_card\n" +
                "WHERE hiveTime > parameter4;";


    }
}
