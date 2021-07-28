package com.vms.metric.analyse.service;

import com.vms.metric.analyse.config.MetricAnalyseConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElsAnalyseESQueryService implements DisposableBean, InitializingBean {

    protected static Logger logger = LoggerFactory.getLogger(ElsAnalyseESQueryService.class);

    private RestHighLevelClient client;
    private static final long TIMEOUT_MILLIS = 180000;
    private static final int SCROLL_ALIVE_TIME_MILLIS = 3000000;

    public Map<String, Object> queryEsData(String indexName,String id){
        try {
            GetRequest getRequest = new GetRequest(indexName, "_doc", id);
            GetResponse getResponse = client.get(getRequest);
            return getResponse.getSourceAsMap();
        }catch (Exception e){
            return null;
        }
    }

    public List<Map<String, Object>> queryEsDataAsList(SearchSourceBuilder sourceBuilder, String... indexName)throws IOException {
        Map dataMap = queryEsData(sourceBuilder, indexName);
        List<Map<String, Object>> dataList;
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (true) {
            String scrollId = (String) dataMap.get("scroll_id");
            if (StringUtils.isEmpty(scrollId))
                break;
            dataList = (List<Map<String, Object>>) dataMap.get("data");
            if (dataList.size() == 0)
                break;

            resultList.addAll(dataList);

            dataList.clear();
            dataMap.clear();
            dataMap = queryEsDataByScrollId(scrollId);
        }
        return resultList;
    }

    public Map<String, Object> queryEsData(SearchSourceBuilder sourceBuilder, String... indexName) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        searchRequest.source(sourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMillis(SCROLL_ALIVE_TIME_MILLIS));

        try {
            SearchResponse response = client.search(searchRequest);
            SearchHits hits = response.getHits();

            List<Map> lists = new ArrayList<>();
            hits.forEach(v -> {
                Map map = v.getSourceAsMap();
                map.put("_id", v.getId());
                map.put("_indexName", v.getIndex());
                lists.add(map);
            });
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("scroll_id", response.getScrollId());
            resultMap.put("data", lists);
            logger.info("queryEsData size:{}", lists.size());
            return resultMap;
        } catch (IOException e) {
            logger.error("queryEsData error ", e);
            throw e;
        }
    }

    public Aggregations queryEsDataWithAgg(String indexName, PipelineAggregationBuilder pipelineAggregationBuilder, AggregationBuilder aggregationBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if (pipelineAggregationBuilder != null)
            sourceBuilder.aggregation(pipelineAggregationBuilder);
        if (aggregationBuilder != null)
            sourceBuilder.aggregation(aggregationBuilder);
        sourceBuilder.timeout(TimeValue.timeValueMillis(TIMEOUT_MILLIS));
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest);
            Aggregations aggregations = response.getAggregations();
            return aggregations;
        } catch (IOException e) {
            logger.error("queryEsDataWithAgg error, ", e);
            throw e;
        }
    }

    public Aggregations queryEsDataWithAgg(SearchSourceBuilder sourceBuilder, String... indexName) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest);
            return response.getAggregations();
        } catch (IOException e) {
            logger.error("queryEsDataWithAgg error, ", e);
            throw e;
        }
    }


    /**
     * query es data by scroll_id
     *
     * @param scroll_id
     * @return
     */
    public Map<String, Object> queryEsDataByScrollId(String scroll_id) throws IOException {
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scroll_id);
        scrollRequest.scroll(TimeValue.timeValueMillis(SCROLL_ALIVE_TIME_MILLIS));

        try {
            SearchResponse response = client.searchScroll(scrollRequest);
            SearchHits hits = response.getHits();

            List<Map> lists = new ArrayList<>();
            hits.forEach(v -> {
                Map map = v.getSourceAsMap();
                map.put("_id", v.getId());
                map.put("_indexName", v.getIndex());
                lists.add(map);
            });
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("scroll_id", response.getScrollId());
            resultMap.put("data", lists);
            logger.info("queryEsDataByScrollId size:{}", lists.size());
            return resultMap;
        } catch (IOException e) {
            logger.error("queryEsDataByScrollId error:{}", e);
            throw e;
        }
    }

    /**
     * update data
     *
     * @param list
     * @return
     */
    public boolean updateEsData(List<UpdateRequest> list) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(v -> bulkRequest.add(v));
        try {
            BulkResponse responses = client.bulk(bulkRequest);
            for (BulkItemResponse bulkItemResponse : responses) {
                if (bulkItemResponse.isFailed()) {
                    logger.warn("elasticsearch getSycResponse fail operate item because of:{},id:{}", bulkItemResponse.getFailureMessage(), bulkItemResponse.getId());
                }
            }
            if (responses.hasFailures())
                return false;
        } catch (IOException e) {
            logger.error("updateEsData error:{}", e);
            throw e;
        }
        return true;
    }

    public void updateEsData(BulkRequest bulkRequest) throws IOException {
        try {
            BulkResponse responses = client.bulk(bulkRequest);
            for (BulkItemResponse bulkItemResponse : responses) {
                if (bulkItemResponse.isFailed())
                    logger.warn("elasticsearch getSycResponse fail operate item because of:{},id:{}", bulkItemResponse.getFailureMessage(), bulkItemResponse.getId());
            }
        } catch (IOException e) {
            logger.error("updateEsData error:{}", e);
            throw e;
        }
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
            HttpHost httpHost = new HttpHost(url, MetricAnalyseConfig.getElsPost(), "http");
            hosts.add(httpHost);
            logger.info("load EsClient :{}", url);
        }
        RestClientBuilder clientBuilder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()])).setMaxRetryTimeoutMillis(300_000);
        client = new RestHighLevelClient(clientBuilder);
    }
}
