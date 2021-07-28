package com.valor.mercury.task.flink.mfc.login;

import com.valor.mercury.task.flink.util.Constants;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Gavin
 * 2019/10/12 17:31
 */
public class DBClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private static DBClient client;
    private LinkedBlockingQueue<LoginActionModel> batchList;
    private List<LoginActionModel> dataList;

    public static DBClient getInstance() throws Exception {
        if (client == null)
            client = new DBClient().open();
        return client;
    }

    private DBClient open() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(Constants.MysqlUrl, Constants.MysqlUserName, Constants.MysqlPassword);
        connection.setAutoCommit(false);
        batchList = new LinkedBlockingQueue<>(300000);
        dataList = new ArrayList<>();
        new Thread(this::dataListMonitor, "dataListMonitor").start();
        return this;
    }

    private void dataListMonitor() {
        while (true) {
            try {
                LoginActionModel value = batchList.poll(20, TimeUnit.SECONDS);
                if (value != null) {
                    dataList.add(value);
                    if (dataList.size() >= 1000)
                        insertOrUpdateUserModel();
                } else if (dataList.size() > 0)
                    insertOrUpdateUserModel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insertOrUpdateUserModel() throws Exception {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO mfc_user(loginType,userId,device,actionTime,vendorId,appVersion,did,countryCode,createTime,macSub) values (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE loginType=?,userId=?,device=?,actionTime=?,vendorId=?,appVersion=?,did=?,countryCode=?,macSub=?");
        for (LoginActionModel model : dataList) {
            //insert
            preparedStatement.setString(1, model.getLoginType());
            preparedStatement.setString(2, model.getUserId());
            preparedStatement.setString(3, model.getDevice());
            preparedStatement.setLong(4, model.getActionTime());
            preparedStatement.setLong(5, model.getVendorId());
            preparedStatement.setLong(6, model.getAppVersion());
            preparedStatement.setString(7, model.getDid());
            preparedStatement.setString(8, model.getCountryCode());
            preparedStatement.setLong(9, System.currentTimeMillis());
            preparedStatement.setString(10, model.getMacSub());
            //update
            preparedStatement.setString(11, model.getLoginType());
            preparedStatement.setString(12, model.getUserId());
            preparedStatement.setString(13, model.getDevice());
            preparedStatement.setLong(14, model.getActionTime());
            preparedStatement.setLong(15, model.getVendorId());
            preparedStatement.setLong(16, model.getAppVersion());
            preparedStatement.setString(17, model.getDid());
            preparedStatement.setString(18, model.getCountryCode());
            preparedStatement.setString(19, model.getMacSub());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        connection.commit();
        dataList.clear();
    }

    public void submitLoginActionModel(LoginActionModel value) throws Exception {
        batchList.put(value);
    }

    public Long queryMFCAppVersion(String sql) {
        try {
            ResultSet rs = connection
                    .prepareStatement(sql)
                    .executeQuery();
            if (rs.next()) {
                return rs.getLong("appVersion");
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() throws Exception {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }
}
