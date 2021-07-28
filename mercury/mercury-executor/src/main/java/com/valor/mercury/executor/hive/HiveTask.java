package com.valor.mercury.executor.hive;

import com.valor.mercury.common.client.HiveClient;
import com.valor.mercury.executor.Task;
import com.valor.mercury.sender.service.MercurySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static com.valor.mercury.common.constant.MercuryConstants.*;

public class HiveTask extends Task {
    private static Logger logger = LoggerFactory.getLogger(HiveTask.class);
    public HiveClient client;
    public String SQL;
    public String[] sqls;
    public String outPutName;

    public void init() {
        taskType = TYPE_OFFLINE_HIVE;
        checkArgs();
        isExecutable = true;
    }

    /**
     * 检查必要参数
     */
    public void checkArgs() {
        Assert.notNull(SQL, "sql is null");
        prepareSql();
        isArgsValid = true;
    }

    public void prepareSql() {
        if (SQL.endsWith(";"))
            SQL = SQL.substring(0, SQL.length() -1);
        sqls = SQL.split(";");
    }

    public void exec() throws Exception {
        Assert.isTrue(isExecutable, "job is not executable");
        for (int i = 0; i < sqls.length; i++) {
            String sql = sqls[i].trim();
            // 最后一条sql 如果是 select查询语句，把结果集发送到对应的 outPutName
            if (i == sqls.length - 1 && (sql.startsWith("select") || sql.startsWith("SELECT"))  && outPutName != null && !outPutName.equals("")) {
                ResultSet resultSet = client.executeQuerySql(sql);
                sendResultSetToMercury(resultSet, outPutName);
                break;
            }
            client.executeSql(sql);
        }
        executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_SUCCESS);
        logger.info("execute job success: {}", this.toString());
    }

    private void sendResultSetToMercury(ResultSet rs, String outPutName) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            int count = 0;
            while (rs.next()) {
                if (count % 2000 == 0) {
                    logger.info("hive sent {} records to mercury", count);
                }
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                MercurySender.put("hive", outPutName, rowData);
                count++;
            }
        } catch (Exception e) {
            logger.error("hive query error", e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                rs = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSql() {
        return SQL;
    }

    public void setSql(String sql) {
        this.SQL = sql;
    }

    public String getOutPutName() {
        return outPutName;
    }

    public void setOutPutName(String outPutName) {
        this.outPutName = outPutName;
    }
}
