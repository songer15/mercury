package com.valor.mercury.script.util;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;

import java.io.IOException;
import java.util.*;

public class ESClient {

    private static String TYPENAME = "_doc";

    private RestHighLevelClient client;

    public ESClient() {
        List<HttpHost> hosts = new ArrayList<>();
        HttpHost httpHost1 = new HttpHost("172.16.0.201", 9200, "http");
        HttpHost httpHost2 = new HttpHost("172.16.0.202", 9200, "http");
        HttpHost httpHost3 = new HttpHost("172.16.0.204", 9200, "http");
        hosts.add(httpHost1);
        hosts.add(httpHost2);
        hosts.add(httpHost3);
        RestClientBuilder clientBuilder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));
        client = new RestHighLevelClient(clientBuilder);
    }

    public Integer write(String indexName, String idField, List<Map> items) throws Exception {
        if (isExistIndex(indexName)) {
            return getSycResponse(getRequest(items, indexName, idField));   //可抛出io异常，表示无法连接到elasticsearch
        } else {
            throw new Exception("create index does not exist");
        }
    }

    /**
     * 根据list集合得到bulkRequest
     *
     * @param lists
     * @return
     */
    private BulkRequest getRequest(List<Map> lists, String indexName, String idField) throws Exception {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            lists.forEach(map -> {
                IndexRequest request;
                if (idField == null || idField.equals(""))
                    request = new IndexRequest(indexName, TYPENAME);
                else
                    request = new IndexRequest(indexName, TYPENAME, map.get(idField) + "");
                Map<String, Object> json = new HashMap<>();
                processCreateTime(json);
                json.putAll(map);
                request.source(json);
                bulkRequest.add(request);
            });
            bulkRequest.timeout(TimeValue.timeValueMillis(6 * 60 * 1000));
            return bulkRequest;
        } catch (Exception e) {
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

    private Integer getSycResponse(BulkRequest request) throws Exception {
        try {
            BulkResponse response = client.bulk(request);
            Iterator<BulkItemResponse> iterator = response.iterator();
            int successTimes = 0;
            while (iterator.hasNext()) {
                BulkItemResponse bulkItemResponse = iterator.next();
                if (!bulkItemResponse.isFailed())
                    successTimes++;
            }
            return successTimes;
        } catch (IOException e) {
            throw e;
        }
    }


    private void processCreateTime(Map<String, Object> json) {
        json.put("LocalCreateTime", new Date());
        json.put("LocalCreateTimestamps", System.currentTimeMillis());
    }

    public void close() throws Exception {
        client.close();
    }
}
