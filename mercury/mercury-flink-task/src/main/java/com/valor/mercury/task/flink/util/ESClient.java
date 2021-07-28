package com.valor.mercury.task.flink.util;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gavin
 * 2019/10/21 18:03
 */
public class ESClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private static ESClient esClient;
    private RestHighLevelClient client;

    public static ESClient getInstance() {
        if (esClient == null)
            esClient = new ESClient().open();
        return esClient;
    }

    private ESClient open() {
        String[] urls = Constants.esHost.split(",");
        List<HttpHost> hosts = new ArrayList<>();
        for (String url : urls) {
            HttpHost httpHost = new HttpHost(url, 9200, "http");
            hosts.add(httpHost);
        }
        RestClientBuilder clientBuilder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));
        client = new RestHighLevelClient(clientBuilder);
        return this;
    }

    public List<Map> queryEsData(SearchSourceBuilder sourceBuilder, String indexName){
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest);
            SearchHits hits = response.getHits();
            List<Map> lists = new ArrayList<>();
            hits.forEach(v -> {
                Map<String,Object> map = v.getSourceAsMap();
                map.put("_id", v.getId());
                map.put("_indexName", v.getIndex());
                lists.add(map);
            });
            return lists;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
