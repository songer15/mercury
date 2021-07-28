package com.valor.mercury.common.client;

import com.valor.mercury.common.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class EsClient {
    private static final Logger logger = LoggerFactory.getLogger(EsClient.class);
    private static final int SCROLL_ALIVE_TIME_MILLIS = 300000;
    private RestHighLevelClient restHighLevelClient;

    public EsClient(List<String> urls) {
        if (urls == null || urls.isEmpty())
            throw new IllegalArgumentException("empty elasticsearch urls");
        HttpHost[] httpHosts = new HttpHost[urls.size()];
        for (int i = 0;  i < urls.size(); i++) {
            httpHosts[i] = HttpHost.create(urls.get(i));
        }
        RestClientBuilder clientBuilder = RestClient.builder(httpHosts);
        this.restHighLevelClient = new RestHighLevelClient(clientBuilder);
    }

    static class BulkWriteResult {
        private int successCount = 0;
        private boolean hasFailure = false;
        private String failureMessage = null;

        public BulkWriteResult() {
        }

        public BulkWriteResult(int successCount, boolean hasFailure, String failureMessage) {
            this.successCount = successCount;
            this.hasFailure = hasFailure;
            this.failureMessage = failureMessage;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public boolean isHasFailure() {
            return hasFailure;
        }

        public void setHasFailure(boolean hasFailure) {
            this.hasFailure = hasFailure;
        }

        public String getFailureMessage() {
            return failureMessage;
        }

        public void setFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
        }
    }

    //Search API
    public SearchRequest prepareSearchRequest(String[] indices, String[] types, QueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.types(types);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    public List<Map<String, Object>> search(String[] indices, String[] types, SearchSourceBuilder searchSourceBuilder) throws IOException{
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.types(types);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest);
        SearchHits hits = response.getHits();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> search(String[] indices, String[] types, SearchSourceBuilder searchSourceBuilder, boolean includeMetaData) throws IOException{
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.types(types);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest);
        SearchHits hits = response.getHits();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            if (includeMetaData) {
                map.put("_id", hit.getId());
                map.put("_index", hit.getIndex());
                map.put("_type", hit.getType());
            }
            list.add(map);
        }
        return list;
    }

    public Tuple<String, List<Map<String, Object>>> searchByScrollId(String[] indices, String[] types, SearchSourceBuilder searchSourceBuilder, String scrollId) throws IOException{
        SearchResponse response;
        if (StringUtils.isEmpty(scrollId)) {
            SearchRequest searchRequest = new SearchRequest(indices);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            searchRequest.source(searchSourceBuilder);
            searchRequest.scroll(TimeValue.timeValueMillis(SCROLL_ALIVE_TIME_MILLIS));
            response = restHighLevelClient.search(searchRequest);
        } else {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMillis(SCROLL_ALIVE_TIME_MILLIS));
            response = restHighLevelClient.searchScroll(scrollRequest);
        }

        SearchHits hits = response.getHits();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            list.add(map);
        }
        return new Tuple<>(response.getScrollId(), list);

    }

    //Document API
    public DocWriteRequest prepareDocWriteRequest(Map map, String index, String type, String id) {
        if (id != null) {
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            updateRequest.doc(map, XContentType.JSON);
            updateRequest.docAsUpsert(true);
            return updateRequest;
        }
        else {
            IndexRequest indexRequest = new IndexRequest(index, type);
            indexRequest.source(map,XContentType.JSON);
            return indexRequest;
        }
    }


    public BulkResponse bulkWrite(List<Map<String, Object>> data, String index, String type, String idField) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, Object> map: data) {
            String id = null;
            if (!StringUtils.isEmpty(idField) && map.get(idField) != null) {
                id = map.get(idField).toString();
            }
            bulkRequest.add(prepareDocWriteRequest(map, index, type, id));
        }
        return  restHighLevelClient.bulk(bulkRequest);
    }

    //Indices API
    public boolean indexExists(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        return restHighLevelClient.indices().exists(request);
    }

    public boolean createIndex(String index, String type, int shards, int replicas, String indexMapping) throws IOException {
        if (indexExists(index))
            return false;
        logger.info("indexMapping: [{}]", indexMapping);
        Map<String, Object> mappings = JsonUtil.jsonToMap(indexMapping);
        mappings.put("AutonomousSystemNumber", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"long\"\n" +
                "    }"));
        mappings.put("AutonomousSystemOrganization", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"keyword\"\n" +
                "    }"));
        mappings.put("CityName", JsonUtil.jsonToMap("{\n" +
                "    \"type\":\"text\",\n" +
                "    \"analyzer\":\"whitespace\"\n" +
                "}"));
        mappings.put("CountryCode", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"keyword\"\n" +
                "    }"));
        mappings.put("GeoHash", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"keyword\"\n" +
                "    }"));
        mappings.put("LocalCreateTime", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"date\"\n" +
                "    }"));
        mappings.put("LocalCreateTimestamps", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"long\"\n" +
                "    }"));
        mappings.put("Location", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"geo_point\"\n" +
                "    }"));
        mappings.put("StateCode", JsonUtil.jsonToMap("{\n" +
                "        \"type\":\"keyword\"\n" +
                "    }"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("properties", mappings);

        CreateIndexRequest request = new CreateIndexRequest(index)
                .settings(Settings.builder().put("index.number_of_shards", shards)
                        .put("index.number_of_replicas", replicas))
                .mapping(type, properties)
                .timeout(TimeValue.timeValueMinutes(2))
                .masterNodeTimeout(TimeValue.timeValueMinutes(2));
        CreateIndexResponse response = restHighLevelClient.indices().create(request);
        return  response.isAcknowledged() && response.isShardsAcknowledged();
    }

    public boolean closeIndices(String... indices) throws IOException {
        CloseIndexRequest request = new CloseIndexRequest(indices);
        CloseIndexResponse response = restHighLevelClient.indices().close(request);
        return  response.isAcknowledged();
    }

    public boolean openIndices(String... indices) throws IOException {
        OpenIndexRequest request = new OpenIndexRequest(indices);
        OpenIndexResponse response = restHighLevelClient.indices().open(request);
        return  response.isAcknowledged();
    }

    public Set<String> getIndicesByPattern(String indexPattern) throws IOException {
        //h=i表示只展示index
        InputStream inputStream = restHighLevelClient.getLowLevelClient()
                .performRequest("GET", "/_cat/indices?h=i&index=" + indexPattern)
                .getEntity()
                .getContent();

        return new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.toSet());
    }

    public String getIndexStatus(String index) throws  IOException {
        //h=s表示只展示status
        InputStream inputStream = restHighLevelClient.getLowLevelClient()
                .performRequest("GET", "/_cat/indices?h=s&index=" + index)
                .getEntity()
                .getContent();

        return new BufferedReader(new InputStreamReader(inputStream))
                .readLine();
    }

    public boolean clearCache(String... indices) throws IOException {
        ClearIndicesCacheRequest request = new ClearIndicesCacheRequest(indices);
        ClearIndicesCacheResponse response = restHighLevelClient.indices().clearCache(request);
        RestStatus restStatus = response.getStatus();
        return RestStatus.OK == restStatus;
    }

    public boolean updateMapping(String index, String type, String indexMapping) throws IOException {
        XContentBuilder mappings = createMappingsFromString(indexMapping, true);
        PutMappingRequest request = new PutMappingRequest(index).type(type).source(mappings);
        PutMappingResponse response = restHighLevelClient.indices().putMapping(request);
        return response.isAcknowledged();
    }

    public XContentBuilder createMappingsFromString(String indexMapping, boolean needExtraFields) throws IOException{
        XContentBuilder xBuilder = XContentFactory.jsonBuilder();
        xBuilder.startObject();
        {
            xBuilder.startObject("properties");
            {
                //如果有，加载常规mapping信息
                if (!StringUtils.isEmpty(indexMapping)) {
                    XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, indexMapping.getBytes());
//                    parser.close();
                    xBuilder.copyCurrentStructure(parser);
                }

                //加载CreateTime Mapping信息
                if (needExtraFields) {
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

                    //加载GeoMapping信息

                    xBuilder.startObject("Location");
                    {
                        xBuilder.field("type", "geo_point");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("CityName");
                    {
                        xBuilder.field("type", "text");
                        xBuilder.field("index", true);
                        xBuilder.field("analyzer", "whitespace");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("StateCode");
                    {
                        xBuilder.field("type", "keyword");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("CountryCode");
                    {
                        xBuilder.field("type", "keyword");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("AutonomousSystemNumber");
                    {
                        xBuilder.field("type", "long");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("AutonomousSystemOrganization");
                    {
                        xBuilder.field("type", "keyword");
                    }
                    xBuilder.endObject();
                    xBuilder.startObject("GeoHash");
                    {
                        xBuilder.field("type", "keyword");
                    }
                    xBuilder.endObject();
                }
            }
            xBuilder.endObject();
        }
        xBuilder.endObject();
        return xBuilder;
    }


    public void close() throws IOException {
        restHighLevelClient.close();
    }


}
