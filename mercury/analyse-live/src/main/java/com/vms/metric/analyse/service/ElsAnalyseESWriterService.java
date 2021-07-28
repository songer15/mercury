package com.vms.metric.analyse.service;

import com.vms.metric.analyse.config.MetricAnalyseConfig;
import com.vms.metric.analyse.model.ElsIndexConfig;
import com.vms.metric.analyse.model.GeoIPData;
import com.vms.metric.analyse.tool.CustomTool;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class ElsAnalyseESWriterService implements DisposableBean, InitializingBean {

    protected static Logger logger = LoggerFactory.getLogger(ElsAnalyseESWriterService.class);
    private static final String TYPENAME = "_doc";

    private RestHighLevelClient client;

    public Integer write(ElsIndexConfig config, List<Map> items) throws Exception {

        int writeNum = 0;
        //判断当前索引是否存在
        if (isExistIndex(config.getIndexName())) {
            writeNum = getSycResponse(getRequest(items, config));   //可抛出io异常，表示无法连接到elasticsearch
        } else {
            logger.info("create Index:{}", config.getIndexName());
            //创建索引
            boolean success = createIndex(config.getIndexName(), TYPENAME,
                    generateBuilder(config.getMappingFile(), config.getNeedGeo()),
                    Settings.builder() //因为有两台主机，设置主分片和副分片各为一
                            .put("index.number_of_shards", 1)
                            .put("index.number_of_replicas", 1));
            if (success) {
                logger.info("write data to elastic");
                writeNum = getSycResponse(getRequest(items, config));   //可抛出io异常，表示无法连接到elasticsearch
            } else {
                logger.error("error to create Index:{}", config.getIndexName());
            }
        }
        return writeNum;
    }



    /**
     * 根据list集合得到bulkRequest
     *
     * @param lists
     * @return
     */
    private BulkRequest getRequest(List<Map> lists, ElsIndexConfig config) throws Exception {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            lists.forEach(map -> {
                IndexRequest request;
                if (config.getIdField() == null || config.getIdField().equals(""))
                    request = new IndexRequest(config.getIndexName(), TYPENAME);
                else
                    request = new IndexRequest(config.getIndexName(), TYPENAME, map.get(config.getIdField()) + "");
                Map<String, Object> json = new HashMap<>();
                //添加坐标数据

                //添加时间数据
                if (config.getNeedTime())
                    processCreateTime(json);
                json.putAll(map);
                request.source(json);
                bulkRequest.add(request);
            });
            bulkRequest.timeout(TimeValue.timeValueMillis(6 * 60 * 1000)); //超时时间6min
            return bulkRequest;
        } catch (Exception e) {
            logger.error("elastic bulk error:{}", e);
            throw e;
        }
    }


    /**
     * 探测当前索引是否存在
     */
    private boolean isExistIndex(String indexName) throws Exception {
        try {
            GetIndexRequest request = new GetIndexRequest();
            request.indices(indexName);
            request.local(false);
            request.humanReadable(true);
            request.includeDefaults(false);
            return client.indices().exists(request);
        } catch (IOException e) {
            throw e;
        }
    }

    //同步方法获取POST/PUT操作返回值
    private boolean getSycResponse(IndexRequest request) throws Exception {
        try {
            IndexResponse response = client.index(request);
            ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    String reason = failure.reason();
                    logger.error("elasticsearch getSycResponse fail:{}", reason);
                }
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("elasticsearch getSycResponse error:{}", e);
        }
        return false;
    }

    /**
     * 创建索引
     */
    private boolean createIndex(String indexName, String typeName, XContentBuilder builder, Settings.Builder settings) throws Exception {
        //没有其他线程使用
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.settings(settings);
            request.mapping(typeName, builder);
            request.timeout(TimeValue.timeValueMinutes(3));//设置等待所有节点确认索引已创建的超时时间为3min
            request.masterNodeTimeout(TimeValue.timeValueMinutes(3)); //设置超时连接到主节点的时间为3min
            CreateIndexResponse response = client.indices().create(request);
            return response.isAcknowledged();
        } catch (IOException e) {
            logger.error("create index error:{}", indexName);
            throw e;
        }
    }

    private XContentBuilder generateBuilder(String path, Boolean needGeo) throws Exception {
        try {
            XContentBuilder xBuilder = XContentFactory.jsonBuilder();
            xBuilder.startObject();
            {
                xBuilder.startObject("properties");
                {
                    //load data json object
                    loadJsonFile(path, xBuilder);

                    //load time date
                    xBuilder.startObject("LocalCreateTimestamps");
                    {
                        xBuilder.field("type", "long");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("LocalCreateTime");
                    {
                        xBuilder.field("type", "date");
                    }
                    xBuilder.endObject();

                    //load geo json object
                    if (needGeo)
                        loadJsonFile("mapping/geo.json", xBuilder);
                }
                xBuilder.endObject();
            }
            xBuilder.endObject();
            return xBuilder;
        } catch (IOException e) {
            logger.error("loading mapping file error:{}", e);
            throw e;
        }
    }

    private void loadJsonFile(String path, XContentBuilder xBuilder) throws IOException {
        List<Map<String, Object>> list = CustomTool.objectMapper.readValue(new FileReader(new File(path)), ArrayList.class);
        for (Map<String, Object> map : list) {
            if (map.containsKey("name")) {
                xBuilder.startObject(map.get("name").toString());
                map.remove("name");
                for (String key : map.keySet()) {
                    xBuilder.field(key, map.get(key));
                }
                xBuilder.endObject();
            } else
                throw new IOException();
        }
    }

    //同步方法获取POST/PUT操作返回值
    private Integer getSycResponse(BulkRequest request) throws Exception {
        try {
            BulkResponse response = client.bulk(request);
            Iterator<BulkItemResponse> iterator = response.iterator();
            int failTimes = 0;
            int successTimes = 0;
            while (iterator.hasNext()) {
                BulkItemResponse bulkItemResponse = iterator.next();
                if (!bulkItemResponse.isFailed()) {
                    successTimes++;
                } else {
                    failTimes++;
                    logger.info("elasticsearch getSycResponse fail operate item because of:{},id:{}", bulkItemResponse.getFailureMessage(), bulkItemResponse.getId());
                }
            }
            logger.info("elasticsearch getSycResponse success operate item number:{}", successTimes);
            logger.info("elasticsearch getSycResponse fail operate item number:{}", failTimes);
            return successTimes;
        } catch (IOException e) {
            logger.error("elasticsearch getSycResponse error:{}", e);
            throw e;
        }
    }

    /**
     * 插入geo相关数据
     */
    private void processGeoData(Map<String, Object> json, GeoIPData geoIPData) {
        if (geoIPData == null) return;
        Map<String, Object> geo = new HashMap<>();
        geo.put("lat", geoIPData.getLatitude());
        geo.put("lon", geoIPData.getLongitude());
        json.put("Location", geo);
        json.put("CityName", geoIPData.getCityName());
        json.put("StateCode", geoIPData.getStateCode());
        json.put("CountryCode", geoIPData.getCountryCode());
        json.put("AutonomousSystemNumber", geoIPData.getAutonomousSystemNumber());
        json.put("GeoHash", geoIPData.getGeohash());
        json.put("AutonomousSystemOrganization", geoIPData.getAutonomousSystemOrganization());
    }

    /**
     * 插入时间相关数据
     */
    private void processCreateTime(Map<String, Object> json) {
        json.put("LocalCreateTime", new Date());
        json.put("LocalCreateTimestamps", System.currentTimeMillis());
    }


    @Override
    public void destroy() throws Exception {
        try {
            client.close();
        } catch (Exception e) {
            logger.error("elasticsearch close error:{}", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> urls = MetricAnalyseConfig.getElsHost();
        List<HttpHost> hosts = new ArrayList<>();
        for (String url : urls) {
            HttpHost httpHost = new HttpHost(url, 9200, "http");
            hosts.add(httpHost);
            logger.info("load EsClient :{}", url);
        }
        RestClientBuilder clientBuilder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));
        client = new RestHighLevelClient(clientBuilder);
    }
}
