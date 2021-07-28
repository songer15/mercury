package com.valor.mercury.manager.test;

import com.valor.mercury.manager.config.ConstantConfig;
import com.valor.mercury.manager.model.ddo.HiveTable;
import com.valor.mercury.manager.tool.JsonUtil;
import org.apache.hive.jdbc.HiveQueryResultSet;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Gavin
 * 2020/8/20 15:59
 */
public class Test3 {
    private Logger logger = LoggerFactory.getLogger(Test3.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    @Test
    public void test() throws Exception {
        ZooKeeper zkClient = new ZooKeeper(ConstantConfig.zkClusterPath(), 15000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
            } else {
//                logger.error("Zookeeper Connect Fail!");
                System.exit(1);
            }
        });
        List<String> childrenList = zkClient.getChildren(ConstantConfig.zkFilePath(), false);
        childrenList.forEach(v -> {
//            logger.info(v);
        });
    }

    @Test
    public void testConnection() throws Exception {
        Connection con;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo?characterEncoding=UTF-8", "root", "1244982742");
            System.out.println("数据库连接成功");
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables");
            logger.info(resultSet.toString());
            while (true) {
                Thread.sleep(3000);
                logger.info("...");
                statement.executeQuery("show tables");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHive() throws Exception {
//        Class.forName("com.mysql.jdbc.Driver");
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        Connection connection = DriverManager.getConnection("jdbc:hive2://51.81.244.223:10000/default;auth=noSasl", "valor", "");
//        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mercury_manager?useUnicode=true&characterEncoding=UTF-8",
//                "root","1244982742");
        Statement statement = connection.createStatement();
//        statement.execute("use default ");
        HiveQueryResultSet resultSet = (HiveQueryResultSet)statement.executeQuery("show tables ");
        while (resultSet.next()) {
            logger.info(resultSet.getString(1));
            String table = resultSet.getString(1);
            HiveTable hiveTable = new HiveTable();
            hiveTable.setStatus("needn't_schedule");
            hiveTable.setName(table);
            Statement statement2 = connection.createStatement();
            HiveQueryResultSet hiveQueryResultSet = (HiveQueryResultSet) statement2.executeQuery("desc formatted  " + table);
            while (hiveQueryResultSet.next()) {
                String colName = hiveQueryResultSet.getString(1);
                String colValue = hiveQueryResultSet.getString(2);
                logger.info(colName+"%"+colValue);
                if ("Database:".equals(colName))
                    hiveTable.setDatabase(colValue.replace(" ", ""));
                if ("Owner:".equals(colName))
                    hiveTable.setOwner(colValue.replace(" ", ""));
                if ("CreateTime:".equals(colName))
                    hiveTable.setCreateTime(new Date());
                if ("Location:".equals(colName))
                    hiveTable.setLocation(colValue.replace(" ", ""));
                if ("Table Type:".equals(colName))
                    hiveTable.setTableType(colValue.replace(" ", ""));
            }
            logger.info(hiveTable.toString());
        }
    }

    @Test
    public void testString() throws Exception {
//        String date = DateTime.now().toString("yyyyMM");
//        logger.info(date);
//        logger.info(sdf.parse("2021-01-18 15:29:07:000").toString());
    }

}
