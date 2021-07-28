package com.valor.mercury.spider.springbatch.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valor.mercury.spider.model.MysqlDataSource;
import com.valor.mercury.spider.model.SpiderJob;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

public abstract class AbstractJdbcReader extends BaseReader implements ItemReader<HashMap<String, Object>> {
    private static Logger logger = LoggerFactory.getLogger(AbstractJdbcReader.class);
    private static Map <MysqlDataSource, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    private JdbcPagingItemReader<HashMap<String, Object>> reader = new JdbcPagingItemReader<>();
    //reader数据源
    private final String TASK_CONFIG_READER_DATASOURCE = "DATASOURCE";
    //reader查询的字段
    private final String TASK_CONFIG_READER_SELECT_CLAUSE="SELECT_CLAUSE";
    //reader查询的表名
    private final String TASK_CONFIG_READER_FROM_CLAUSE="FROM_CLAUSE";
    //reader分页查询唯一标识
    private final String TASK_CONFIG_READER_SORT_KEYS="SORT_KEYS";
    //reader分页查询判断语句
    private final String TASK_CONFIG_READER_WHERE_CLAUSE="WHERE_CLAUSE";
    //reader分页查询每页数据大小
    private final String TASK_CONFIG_READER_PAGE_SIZE="PAGE_SIZE";
    private ObjectMapper objectMapper = new ObjectMapper();
    abstract public Map<String, Object> getParamsMap();

    @Override
    public void init(SpiderJob spiderJob) throws Exception {
        this.spiderJob = spiderJob;
        reader.setRowMapper(new HashMapRowMapper());
        MysqlDataSource mysqlDataSource = objectMapper.readValue((String) spiderJob.configMap.get(TASK_CONFIG_READER_DATASOURCE), new TypeReference<MysqlDataSource>(){});
        BasicDataSource dataSource;
        if (dataSourceMap.containsKey(mysqlDataSource)) {
            dataSource = (BasicDataSource) dataSourceMap.get(mysqlDataSource);
        } else {
            dataSource = new BasicDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(mysqlDataSource.url);
            dataSource.setUsername(mysqlDataSource.userName);
            dataSource.setPassword(mysqlDataSource.password);
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setPoolPreparedStatements(true);
            dataSource.setInitialSize(1);
            dataSource.setMaxActive(20);
            dataSource.setMaxIdle(1);
            dataSource.setMinIdle(0);
            dataSource.setMaxWait(5000);
//        空闲连接被释放前的最小空闲时间,也就是最少要空闲这个时间，空闲连接才有可能被回收
            dataSource.setMinEvictableIdleTimeMillis(180000);
            //清除空闲连接的定时任务执行间隔
            dataSource.setTimeBetweenEvictionRunsMillis(180000);
            dataSource.setNumTestsPerEvictionRun(2);
            dataSource.setTestOnBorrow(true);
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnReturn(true);
            dataSource.setDefaultTransactionIsolation(TRANSACTION_READ_COMMITTED);
            dataSourceMap.put(mysqlDataSource, dataSource);
            logger.info("init dataSource: [{}]", mysqlDataSource.toString());
        }

        reader.setDataSource(dataSource);
        //设置分页大小
        Double pageSize = (Double) spiderJob.configMap.get(TASK_CONFIG_READER_PAGE_SIZE);
        Assert.notNull(pageSize, "pageSize is null");
        reader.setPageSize(pageSize.intValue());
        //配置查询参数
        String selectClause = (String) spiderJob.configMap.get(TASK_CONFIG_READER_SELECT_CLAUSE);
        Assert.notNull(selectClause, "selectClause is null");
        String fromClause = (String) spiderJob.configMap.get(TASK_CONFIG_READER_FROM_CLAUSE);
        Assert.notNull(fromClause, "fromClause is null");
        String sortKeys = (String) spiderJob.configMap.get(TASK_CONFIG_READER_SORT_KEYS);
        Assert.notNull(sortKeys, "sortKeys is null");
        Map<String, Order> orderMap = Arrays.stream(sortKeys.split(","))
                .collect(LinkedHashMap::new, (c, v) -> c.put(v, Order.ASCENDING), LinkedHashMap::putAll);
        Assert.notEmpty(orderMap, "orderMap is empty");

        //设置QueryProvider
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause(selectClause);
        provider.setFromClause(fromClause);
        provider.setSortKeys(orderMap);

        //设置增量标记
        String whereClause = (String) spiderJob.configMap.get(TASK_CONFIG_READER_WHERE_CLAUSE);
        if (whereClause != null) {
            reader.setParameterValues(getParamsMap());
            provider.setWhereClause(whereClause);
        }
        reader.setQueryProvider(provider);
        reader.afterPropertiesSet();
    }


    @Override
    public HashMap<String, Object> read() throws Exception{
        spiderJob.readNumber++;
        return reader.read();
    }

    class HashMapRowMapper implements RowMapper<HashMap<String, Object>> {

        @Override
        public HashMap<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            HashMap<String, Object> map = new HashMap<>();
            ResultSetMetaData rd = rs.getMetaData();
            int count = rd.getColumnCount();
            for (int i = 1; i <= count; i++) {
                map.put(rd.getColumnName(i), rs.getObject(i));
            }
            return map;
        }
    }
}
