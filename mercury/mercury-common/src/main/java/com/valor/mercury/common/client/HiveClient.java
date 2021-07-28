package com.valor.mercury.common.client;



import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class HiveClient {
    private  String url;
    protected final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Connection conn = null;

    public HiveClient(String HDFSUser, String url) {
        try {
            System.setProperty("HADOOP_USER_NAME", HDFSUser);
            this.url = url;
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            conn = DriverManager.getConnection(url);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public void executeSql(String sql) throws Exception {
        logger.info("execute sql:  [{}]", sql);
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    public ResultSet executeQuerySql(String sql) throws Exception {
        logger.info("execute query sql:  [{}]", sql);
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

//    public List<String> getAllDatabases() {
//        List<String> databases = null;
//        try {
//            databases = client.getAllDatabases();
//        } catch (TException ex) {
//            logger.error(ex.getMessage());
//        }
//        return databases;
//    }
//
//    public Database getDatabase(String db) {
//        Database database = null;
//        try {
//            database = client.getDatabase(db);
//        } catch (TException ex) {
//            logger.error(ex.getMessage());
//        }
//        return database;
//    }
//
//    public List<FieldSchema> getSchema(String db, String table) {
//        List<FieldSchema> schema = null;
//        try {
//            schema = client.getSchema(db, table);
//        } catch (TException ex) {
//            logger.error(ex.getMessage());
//        }
//        return schema;
//    }
//
//    public List<String> getAllTables(String db) {
//        List<String> tables = null;
//        try {
//            tables = client.getAllTables(db);
//        } catch (TException ex) {
//            logger.error(ex.getMessage());
//        }
//        return tables;
//    }
//
//    public String getLocation(String db, String table) {
//        String location = null;
//        try {
//            location = client.getTable(db, table).getSd().getLocation();
//        }catch (TException ex) {
//            logger.error(ex.getMessage());
//        }
//        return location;
//    }
}
