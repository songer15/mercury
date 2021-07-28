package com.valor.mercury.manager.service;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.valor.mercury.manager.config.ConstantConfig;
import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.HiveTable;
import com.valor.mercury.manager.model.system.PageResult;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.hive.jdbc.HiveQueryResultSet;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @author Gavin
 * 2020/10/22 13:44
 */
@Service
public class HiveService extends BaseDBService {
    private final MercuryManagerDao dao;
    private final LogAlarmService logAlarmService;
    private final HAService haService;
    private static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private final static int retryTimes = 2;
    private Connection connection;
    private String currentDateBase = "";
    public Long lastActionTime = System.currentTimeMillis();
    private CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));

    @Autowired
    public HiveService(MercuryManagerDao dao, LogAlarmService logAlarmService, HAService haService) {
        this.dao = dao;
        this.logAlarmService = logAlarmService;
        this.haService = haService;
    }

//    @Scheduled(fixedDelay = 180_000, initialDelay = 60_000)
    private void hiveSynchronized() {
        logger.info("hive synchronized start!");
        if (haService.getNodeStatus().equals(HAService.NodeStatus.LEADER)) {
            List<HiveTable> list = listEntity(HiveTable.class, "database", Restrictions.eq("status", "need_schedule"));
            for (HiveTable table : list) {
                Date scheduledDate = table.getNextExecuteTime();
                if (scheduledDate == null) {
                    ZonedDateTime now = ZonedDateTime.now();
                    ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(table.getCron()));
                    executionTime.nextExecution(now).ifPresent(nextExecution ->
                            table.setNextExecuteTime(Date.from(nextExecution.toInstant())));
                } else if (Math.abs(System.currentTimeMillis() - scheduledDate.getTime()) <= 120_000
                        || (System.currentTimeMillis() - scheduledDate.getTime() > 120_000)) {
                    logger.info("hive synchronized start:", table.toString());
                    logAlarmService.addLog(MercuryConstants.LOG_LEVEL_INFO, this.getClass().getName(), "hive", "hive synchronized:" + table.getName());
                    try {
                        if (connection == null || connection.isClosed())
                            connection = createConnection(table.getDatabase());
                        Statement statement = connection.createStatement();
                        lastActionTime = System.currentTimeMillis();
                        if (!table.getDatabase().equals(currentDateBase)) {
                            statement.execute("use " + table.getDatabase());
                            currentDateBase = table.getDatabase();
                        }
                        if (!statement.execute(table.getCommand())) {
                            logger.error("hive synchronized fail:{}", table.getName());
                            logAlarmService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "hive table:" + table.getName(), "hive synchronized fail:" + table.toString());
                        }
                        statement.close();
                    } catch (Exception e) {
                        logger.error("hive synchronized error:{}", e);
                        logAlarmService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "hive table:" + table.getName(), "hive synchronized error:" + e.getMessage());
                    }
                    Optional<ZonedDateTime> optional = ExecutionTime.forCron(parser.parse(table.getCron()))
                            .nextExecution(ZonedDateTime.of(
                                    LocalDateTime.ofInstant(scheduledDate.toInstant(), ZoneId.systemDefault()), ZoneId.systemDefault()));
                    if (optional.isPresent()) {
                        Date nextScheduledDate = Date.from(optional.get().toInstant());  //下次计划生成任务的时间
                        table.setNextExecuteTime(nextScheduledDate);
                        table.setLastExecuteTime(scheduledDate);
                        update(table);
                    } else {
                        logger.error("hive synchronized fail, cron error {}", table.toString());
                        logAlarmService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "hive table:" + table.getName(), "hive synchronized fail,cron error :" + table.toString());
                    }
                }
            }
        } else if (haService.getNodeStatus().equals(HAService.NodeStatus.LOOKING)) {
            try {
                logger.info("waiting for elect master node");
                Thread.sleep(60_000);
                hiveSynchronized();
            } catch (Exception e) {
                logger.error("waiting for elect master node error:{}", e);
                hiveSynchronized();
            }
        }
    }

    public PageResult<HiveTable> listTables(Integer page, Integer limit, String database) {
        return list(HiveTable.class, page, limit, Restrictions.eq("database", database));
    }

    public ImmutablePair<Boolean, String> refreshHiveTables(String database, int times) {
        if (times >= retryTimes) {
            logAlarmService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "hive",
                    "connect hive fail");
            logger.error("connect hive fail");
            return ImmutablePair.of(false, "connect hive fail");
        }
        try {
            if (connection == null || connection.isClosed())
                connection = createConnection(database);
            Statement statement = connection.createStatement();
            lastActionTime = System.currentTimeMillis();
            if (!database.equals(currentDateBase)) {
                statement.execute("use " + database);
                currentDateBase = database;
            }
            List<HiveTable> list = dao.listEntity(HiveTable.class, Restrictions.eq("database", database));
            Map<String, HiveTable> tables = list.stream().collect(HashMap::new, (c, v) -> c.put(v.getName(), v), HashMap::putAll);
            HiveQueryResultSet resultSet = (HiveQueryResultSet) statement.executeQuery("show tables");
            Thread.sleep(1000L);
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                Statement detailStatement = connection.createStatement();
                HiveQueryResultSet hiveQueryResultSet = (HiveQueryResultSet) detailStatement.executeQuery("desc formatted  " + tableName);
                if (!tables.containsKey(tableName) || "deleted".equals(tables.get(tableName).getStatus())) {
                    HiveTable table = tables.containsKey(tableName) ? tables.get(tableName) : new HiveTable();
                    table.setStatus("needn't_schedule");
                    table.setName(tableName);
                    table.setDatabase(database);
                    while (hiveQueryResultSet.next()) {
                        String colName = hiveQueryResultSet.getString(1);
                        String colValue = hiveQueryResultSet.getString(2);
                        if ("Database:           ".equals(colName))
                            table.setDatabase(colValue.replace(" ", ""));
                        if ("Owner:              ".equals(colName))
                            table.setOwner(colValue.replace(" ", ""));
                        if ("CreateTime:         ".equals(colName))
                            table.setCreateTime(sdf.parse(colValue));
                        if ("Location:           ".equals(colName))
                            table.setLocation(colValue.replace(" ", ""));
                        if ("Table Type:         ".equals(colName))
                            table.setTableType(colValue.replace(" ", ""));
                    }
                    table.setLocalCreateTime(new Date());
                    if (table.getId() == null)
                        dao.saveEntity(table);
                    else
                        dao.updateEntity(table);
                } else
                    tables.remove(tableName);
                detailStatement.close();
            }
            tables.forEach((k, v) -> {
                v.setStatus("deleted");
                update(v);
            });
            statement.close();
        } catch (CommunicationsException e) {
            //连接错误
            connection = null;
            refreshHiveTables(database, ++times);
        } catch (MySQLSyntaxErrorException e) {
            //SQL错误
            logger.error("refreshHiveTables error:{}", e);
            return ImmutablePair.of(false, "SQL错误：" + e.getMessage());
        } catch (Exception e) {
            //其他错误
            logger.error("refreshHiveTables error:{}", e);
            return ImmutablePair.of(false, e.getMessage());
        }
        return ImmutablePair.of(true, null);
    }

    private Connection createConnection(String database) throws Exception {
        Class.forName(driverName);
        currentDateBase = database;
        String path = ConstantConfig.hiveUrl().endsWith("/") ? ConstantConfig.hiveUrl() + database + ";auth=noSasl" : ConstantConfig.hiveUrl() + "/" + database + ";auth=noSasl";
        return DriverManager.getConnection(path, ConstantConfig.hiveUserName(), ConstantConfig.hivePassword());
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (Exception e) {
            logger.error("closeConnection error:{}", e);
        }
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    public ImmutablePair<Boolean, String> addTable(String database, String command) {
        try {
            if (connection == null || connection.isClosed())
                connection = createConnection(database);
            Statement statement = connection.createStatement();
            lastActionTime = System.currentTimeMillis();
            if (!database.equals(currentDateBase)) {
                statement.execute("use " + database);
                currentDateBase = database;
            }
            statement.execute(command);
            refreshHiveTables(database, 0);
            return ImmutablePair.of(true, null);
        } catch (CommunicationsException e) {
            //连接错误
            logger.error("addTable error:{}", e);
            return ImmutablePair.of(false, "连接错误：" + e.getMessage());
        } catch (MySQLSyntaxErrorException e) {
            //SQL错误
            logger.error("addTable error:{}", e);
            return ImmutablePair.of(false, "SQL错误：" + e.getMessage());
        } catch (Exception e) {
            //其他错误
            logger.error("addTable error:{}", e);
            return ImmutablePair.of(false, e.getMessage());
        }
    }

    public ImmutablePair<Boolean, String> dropTable(String database, Long tableId) {
        try {
            HiveTable table = getEntityById(HiveTable.class, tableId);
            if (connection == null || connection.isClosed())
                connection = createConnection(database);
            Statement statement = connection.createStatement();
            lastActionTime = System.currentTimeMillis();
            if (!database.equals(currentDateBase)) {
                statement.execute("use " + database);
                currentDateBase = database;
            }
            statement.execute("drop table " + table.getName());
            deleteEntity(table);
            return ImmutablePair.of(true, null);
        } catch (CommunicationsException e) {
            //连接错误
            logger.error("dropTable error:{}", e);
            return ImmutablePair.of(false, "连接错误：" + e.getMessage());
        } catch (MySQLSyntaxErrorException e) {
            //SQL错误
            logger.error("dropTable error:{}", e);
            return ImmutablePair.of(false, "SQL错误：" + e.getMessage());
        } catch (Exception e) {
            //其他错误
            logger.error("dropTable error:{}", e);
            return ImmutablePair.of(false, e.getMessage());
        }
    }

    public ImmutablePair<Boolean, String> editHiveTable(Long tableId, String cron, String command) {
        HiveTable table = getEntityById(HiveTable.class, tableId);
        if (table != null) {
            table.setCron(cron);
            table.setCommand(command);
            if (Strings.isNotEmpty(cron)) {
                try {
                    table.setStatus("need_schedule");
                    ZonedDateTime now = ZonedDateTime.now();
                    ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(cron));
                    executionTime.nextExecution(now).ifPresent(nextExecution ->
                            table.setNextExecuteTime(Date.from(nextExecution.toInstant())));
                }catch (Exception e){
                    logger.error("wrong cron expression:{}",cron);
                    return ImmutablePair.of(false, "wrong cron expression");
                }
            } else
                table.setStatus("needn't_schedule");
            update(table);
            return ImmutablePair.of(true, null);
        } else
            return ImmutablePair.of(false, "empty entity");
    }
}
