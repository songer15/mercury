package com.valor.mercury.elasticsearch.web.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.valor.mercury.elasticsearch.web.configs.ElasticsearchWebConfiguration;
import com.valor.mercury.elasticsearch.web.controller.ResponseHttpCode;
import com.valor.mercury.elasticsearch.web.model.APIException;
import com.valor.mercury.elasticsearch.web.model.indexState.IndexState;
import com.valor.mercury.elasticsearch.web.model.request.ConditionQueryRequest;
import com.valor.mercury.elasticsearch.web.model.response.ElasticsearchDataResponse;
import com.valor.mercury.elasticsearch.web.model.response.IndexStateResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ElasticsearchService {
    private static Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    private Map<String, JsonObject> indexStatsCacheMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> indexMappingCacheMap = new ConcurrentHashMap<>();
    private RestHighLevelClient restHighLevelClient;
    private JsonParser jsonParser = new JsonParser();

    private Gson gson = new Gson();

    @Autowired
    private ElasticsearchWebConfiguration configuration;


    @PostConstruct
    void init() {
        List<String> addressList = configuration.getElasticsearchAddress();
        HttpHost[] elasticsearchHosts = new HttpHost[addressList.size()];
        for (int i = 0; i < addressList.size(); i++) {
            elasticsearchHosts[i] = HttpHost.create(addressList.get(i));
        }

        RestClientBuilder restClientBuilder = RestClient.builder(elasticsearchHosts);
        this.restHighLevelClient = new RestHighLevelClient(restClientBuilder);

        refreshElasticsearchCache();

    }

    @Scheduled(fixedDelay = 300 * 1000L)
    private void refreshElasticsearchCache() {
        refreshIndexStatsCache();
        refreshIndexMappingCacheMap();
    }

    private void refreshIndexMappingCacheMap() {
        try {
            Response response = restHighLevelClient.getLowLevelClient().performRequest("Get", String.format("_mapping/_doc"));
            JsonObject indexsMapping = jsonParser.parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();

            Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
            for (Map.Entry<String, JsonElement> indexEntry : indexsMapping.entrySet()) {
                String index = indexEntry.getKey();
                JsonObject indexJsonObject = indexEntry.getValue().getAsJsonObject();

                Map<String, String> indexMap = new HashMap<>();
                if (!indexJsonObject.has("mappings"))
                    continue;
                JsonObject mappings = indexJsonObject.getAsJsonObject("mappings");
                if (!mappings.has("_doc"))
                    continue;
                JsonObject doc = mappings.getAsJsonObject("_doc");
                if (!doc.has("properties"))
                    continue;
                JsonObject properties = doc.getAsJsonObject("properties");
                for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
                    JsonObject value = entry.getValue().getAsJsonObject();
                    if (value == null || !value.has("type")) {
                        //logger.warn("Index [{}] ,field [{}]", index, entry.getKey());
                    } else
                        indexMap.put(entry.getKey(), entry.getValue().getAsJsonObject().get("type").getAsString());
                }

                map.put(index, indexMap);
            }
            this.indexMappingCacheMap = map;
            logger.info("Refresh [{}] index mappings.", indexMappingCacheMap.size());
        } catch (Exception e) {
            logger.error("refreshIndexMappingCacheMap error.", e);
        }
    }

    private void refreshIndexStatsCache() {
        try {
            Map<String, JsonObject> cache = new ConcurrentHashMap<>();
            Response response = this.restHighLevelClient.getLowLevelClient().performRequest("Get", "/_stats");
            JsonObject stats = jsonParser.parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();
            JsonObject indices = stats.getAsJsonObject("indices");
            for (Map.Entry<String, JsonElement> entry : indices.entrySet()) {
                cache.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
            this.indexStatsCacheMap = cache;
            logger.info("Refresh [{}] index stats.", indexStatsCacheMap.size());
        } catch (Exception e) {
            logger.error("refreshIndexCache error.", e);
        }
    }


    public List<IndexStateResponse> searchIndexs(String index) {
        List<IndexStateResponse> responses = new ArrayList<>();

        for (Map.Entry<String, JsonObject> entry : indexStatsCacheMap.entrySet()) {
            if (!StringUtils.isEmpty(index) && !StringUtils.containsIgnoreCase(entry.getKey(), index))
                continue;
            if (!indexMappingCacheMap.containsKey(entry.getKey()))
                continue;
            IndexStateResponse response = new IndexStateResponse();
            response.setIndex(entry.getKey());
            response.setIndexPrimaries(IndexState.toIndexState(entry.getValue().getAsJsonObject("primaries")));
            response.setIndexTotal(IndexState.toIndexState(entry.getValue().getAsJsonObject("total")));
            responses.add(response);
        }

        Collections.sort(responses, Comparator.comparing(IndexStateResponse::getIndex));
        return responses;
    }

    public Map<String, String> searchIndexMapping(String index) throws APIException {

        Map<String, String> ret = indexMappingCacheMap.get(index);
        if (ret == null)
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS_MSG);
        return ret;
    }


    public ElasticsearchDataResponse dataView(String index) throws APIException {
        if (!indexStatsCacheMap.containsKey(index))
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_DATA_VIEW, ResponseHttpCode.ERR_ELASTICSEARCH_DATA_VIEW_INDEX_NOT_EXISTS, ResponseHttpCode.ERR_ELASTICSEARCH_DATA_VIEW_INDEX_NOT_EXISTS_MSG);

        try {

            ElasticsearchDataResponse response = new ElasticsearchDataResponse();
            Map<String, String> indexMap = indexMappingCacheMap.get(index);
            if (indexMap == null)
                return response;

            response.getColumns().addAll(indexMap.keySet());

            SortBuilder sortBuilder = null;
            if (indexMap.containsKey(configuration.getElasticsearchDateField()))
                sortBuilder = new FieldSortBuilder(configuration.getElasticsearchDateField()).order(SortOrder.DESC);
            SearchResponse searchResponse = elasticsearch(index, QueryBuilders.matchAllQuery(), sortBuilder, configuration.getElasticsearchDataViewSize());

            SearchHit[] searchHits = searchResponse.getHits().getHits();
            if (searchHits == null || searchHits.length == 0)
                return response;
            response.setTotal(searchResponse.getHits().getTotalHits());
            for (SearchHit searchHit : searchHits) {
                response.getDatas().add(searchHit.getSourceAsMap());
            }

            return response;

        } catch (Exception e) {
            logger.error("Elasticsearch data view [{}] error.", index, e);
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_DATA_VIEW, ResponseHttpCode.SERVER_ERROR, ResponseHttpCode.SERVER_ERROR_MSG);
        }
    }


    public ElasticsearchDataResponse basicQuery(String index, String requestJsonStr) throws Exception {
        if (!indexStatsCacheMap.containsKey(index))
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS_MSG);
        if (!indexMappingCacheMap.containsKey(index))
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS_MSG);
        Map<String, String> indexMap = indexMappingCacheMap.get(index);

        ElasticsearchDataResponse response = new ElasticsearchDataResponse();
        response.getColumns().addAll(indexMap.keySet());

        List<ConditionQueryRequest> queryRequests = gson.fromJson(requestJsonStr, new TypeToken<ArrayList<ConditionQueryRequest>>() {
        }.getType());

        //判断查询是否包含日期限制
        if (indexMap.containsKey(configuration.getElasticsearchDateField())) {
            boolean hasDate = false;
            for (ConditionQueryRequest request : queryRequests) {
                if (request.getConditionFieldVal().equals(configuration.getElasticsearchDateField())) {
                    hasDate = true;
                    break;
                }
            }
            if (!hasDate) {
                throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_DATE_NOT_EXISTS, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_DATE_NOT_EXISTS_MSG);
            }
        }


        try {
            QueryBuilder queryBuilder = builderQueryBuilder(queryRequests, indexMap);

            SortBuilder sortBuilder = null;
            if (indexMap.containsKey(configuration.getElasticsearchDateField()))
                sortBuilder = new FieldSortBuilder(configuration.getElasticsearchDateField()).order(SortOrder.DESC);

            SearchResponse searchResponse = elasticsearch(index, queryBuilder, sortBuilder, configuration.getElasticsearchBasicQuerySize());

            SearchHit[] searchHits = searchResponse.getHits().getHits();
            if (searchHits == null || searchHits.length == 0)
                return response;

            response.setTotal(searchResponse.getHits().getTotalHits());
            for (SearchHit searchHit : searchHits) {
                response.getDatas().add(searchHit.getSourceAsMap());
            }

            return response;
        } catch (Exception e) {
            if (e instanceof APIException)
                throw e;
            logger.error("Elasticsearch basic query [{}][{}] error.", index, requestJsonStr, e);

            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.SERVER_ERROR, ResponseHttpCode.SERVER_ERROR_MSG);
        }
    }


    private QueryBuilder builderQueryBuilder(List<ConditionQueryRequest> queryRequests, Map<String, String> indexMapping) throws ParseException, APIException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (ConditionQueryRequest queryRequest : queryRequests) {
            if (queryRequest.getLogicalOperator().equals("and")) {
                String fieldType = indexMapping.get(queryRequest.getConditionFieldVal());
                if (StringUtils.isEmpty(fieldType))
                    continue;
                if (queryRequest.getConditionOptionVal().equals("equal")) {
                    QueryBuilder subQueryBuilder = null;
                    if (fieldType.equals("date")) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
                        DateTime dateTime = DateTime.parse(queryRequest.getConditionValueVal().getValue(), dateTimeFormatter);
                        Date date = dateTime.toDate();
                        Date nextDate = dateTime.plusDays(1).toDate();
                        subQueryBuilder = QueryBuilders.rangeQuery(queryRequest.getConditionFieldVal()).from(date, true).to(nextDate, false);
                    } else
                        subQueryBuilder = QueryBuilders.termQuery(queryRequest.getConditionFieldVal(), queryRequest.getConditionValueVal().getValue());

                    queryBuilder.must(subQueryBuilder);
                } else if (queryRequest.getConditionOptionVal().equals("like"))
                    queryBuilder.must(QueryBuilders.wildcardQuery(queryRequest.getConditionFieldVal(), "*" + queryRequest.getConditionValueVal().getValue() + "*"));
                else if (queryRequest.getConditionOptionVal().equals("unequal"))
                    queryBuilder.mustNot(QueryBuilders.termQuery(queryRequest.getConditionFieldVal(), queryRequest.getConditionValueVal().getValue()));
                else if (queryRequest.getConditionOptionVal().equals("between")) {
                    QueryBuilder subQueryBuilder = null;
                    if (fieldType.equals("date")) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
                        DateTime from = DateTime.parse(queryRequest.getConditionValueLeftVal().getValue(), dateTimeFormatter);
                        DateTime to = DateTime.parse(queryRequest.getConditionValueRightVal().getValue(), dateTimeFormatter).plusDays(1);
                        if (Math.abs(Days.daysBetween(from, to).getDays()) > configuration.getElasticsearchBasicQueryDays())
                            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_DAYS_TOO_LONG, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_DAYS_TOO_LONG_MSG);
                        subQueryBuilder = QueryBuilders.rangeQuery(queryRequest.getConditionFieldVal()).from(from.toDate(), true).to(to.toDate(), false);
                    } else
                        subQueryBuilder = QueryBuilders.termQuery(queryRequest.getConditionFieldVal(), queryRequest.getConditionValueVal().getValue());
                    queryBuilder.must(subQueryBuilder);
                }
            }
        }

        return queryBuilder;
    }

    private SearchResponse elasticsearch(String index, QueryBuilder queryBuilder, SortBuilder sortBuilder, int querySize) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        if (sortBuilder != null)
            searchSourceBuilder.sort(sortBuilder);
        searchSourceBuilder.from(0).size(querySize).timeout(TimeValue.timeValueMinutes(1));
        searchRequest.indices(index).source(searchSourceBuilder);
        return restHighLevelClient.search(searchRequest);
    }
}
