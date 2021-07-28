package com.valor.mercury.elasticsearch.web;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElasticsearchAPITest {

    private RestHighLevelClient restHighLevelClient;
    private String elasticsearchServerAddress = "192.168.196.252:9200";
    private Gson gson = new Gson();

    @Before
    public void init() {

        String[] address = elasticsearchServerAddress.split(",");
        HttpHost[] elasticsearchHosts = new HttpHost[address.length];
        for (int i = 0; i < address.length; i++) {
            elasticsearchHosts[i] = HttpHost.create(address[i]);
        }

        RestClientBuilder restClientBuilder = RestClient.builder(elasticsearchHosts);
        this.restHighLevelClient = new RestHighLevelClient(restClientBuilder);
    }

    @After
    public void close() throws IOException {
        this.restHighLevelClient.close();
    }

    @Test
    public void testGetInfoApi() throws IOException {
        MainResponse response = restHighLevelClient.info();
        System.out.println(gson.toJson(response));
    }

    @Test
    public void testGetClusterStats() throws IOException {
        Response response = restHighLevelClient.getLowLevelClient().performRequest("Get", "/_cluster/stats");

        System.out.println(EntityUtils.toString(response.getEntity()));
    }


    @Test
    public void testGetIndexs() throws IOException {
        Response response = restHighLevelClient.getLowLevelClient().performRequest("Get", "/_stats");
        System.out.println(EntityUtils.toString(response.getEntity()));
    }


    @Test
    public void testSearch() throws IOException {
        String index = "test_zk_dd2";

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0).size(50).timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.sort(new FieldSortBuilder("LocalCreateTime").order(SortOrder.DESC));
        searchRequest.indices(index).source(searchSourceBuilder);


        SearchResponse response = restHighLevelClient.search(searchRequest);
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(gson.toJson(hit.getSourceAsMap()));

        }
    }

    @Test
    public void testGetIndexMapping() throws IOException {
        String index = "test_zk_dd2";
        Response response = restHighLevelClient.getLowLevelClient().performRequest("Get", String.format("/%s/_mapping/_doc",index));
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetIndexsMapping() throws IOException {
        Response response = restHighLevelClient.getLowLevelClient().performRequest("Get", String.format("_mapping/_doc"));
        System.out.println(EntityUtils.toString(response.getEntity()));
    }


    @Test
    public void testConditionSearch() throws IOException {
        String index = "test_zk_dd2";

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.must(QueryBuilders.wildcardQuery("CityName","*23*"));
        queryBuilder.must(QueryBuilders.rangeQuery("LocalCreateTime").format("yyyy-MM-dd").gt("2021-03-10"));
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.sort(new FieldSortBuilder("LocalCreateTime").order(SortOrder.DESC));
        searchSourceBuilder.from(0).size(50).timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.indices(index).source(searchSourceBuilder);


        SearchResponse response = restHighLevelClient.search(searchRequest);
        System.out.println(response.getHits().getTotalHits());
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(gson.toJson(hit.getSourceAsMap()));

        }
    }
}
