package com.vms.metric.analyse.service.job.live;


import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.ElsAnalyseESQueryService;
import com.vms.metric.analyse.service.BaseDataAnalyse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 统计日活用户
 */
@Component
public class DAUAnalyse implements BaseDataAnalyse {
    @Autowired
    private ElsAnalyseESQueryService queryService;
    protected static Logger logger = LoggerFactory.getLogger(DAUAnalyse.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private List<Map> billUpdateList  = new ArrayList<>();

    @Override
    public WorkItem execute(WorkItem workItem)  {
        workItem.setStartTime(new Date());
        String startDate = workItem.getPre();
        Asserts.notNull(startDate, "startDate is null");
        String endDate = sdf.format(new Date());
        Asserts.notNull(endDate, "endDate is null");
        try {
            List<String> products = new ArrayList<>();
            try {
                FileReader fr = new FileReader("cfg/dau.txt");
                BufferedReader bf = new BufferedReader(fr);
                String str;
                // 按行读取字符串
                while (true) {
                    str = bf.readLine();
                    if (str == null || StringUtils.isEmpty(str.trim()))
                        break;
                    products.add(str);
                }
                bf.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String s : products) {
                try {
                    String [] productInfo = s.split("\\s+");
                    String productId = productInfo[0];
                    String releaseId = productInfo[1];
                    String index = productInfo[2];
                    String cardinalityField = productInfo[3];
                    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                    logger.info("executing dau task: {} {} {} {}", productId, releaseId, index, cardinalityField);
                    //按天聚合 cardinality，也就是日活
                    AggregationBuilder aggregationBuilder = AggregationBuilders
                            .dateHistogram("date")
                            .field("LocalCreateTime")
                            .interval(1000 * 60 * 60 * 24)
                            .timeZone(DateTimeZone.forOffsetHours(8))
                            .minDocCount(1).subAggregation(
                                    AggregationBuilders
                                            .cardinality("dau")
                                            .field(cardinalityField));

                    QueryBuilder queryBuilder =  QueryBuilders
                            .boolQuery()
                            .must(QueryBuilders
                                    .rangeQuery("LocalCreateTime")
                                    .timeZone("Asia/Shanghai")
                                    .gte(startDate)
                                    .lte(endDate)
                                    .format("strict_date"))
                            .must(QueryBuilders.matchPhraseQuery("release_id", releaseId));
                    sourceBuilder.query(queryBuilder);
                    sourceBuilder.aggregation(aggregationBuilder);
                    sourceBuilder.timeout(TimeValue.timeValueMillis(5 * 60_000));
                    sourceBuilder.size(2000);

                    Aggregations aggregations = queryService.queryEsDataWithAgg(sourceBuilder, index);
                    Map<String, Aggregation> aggMap = aggregations != null ? aggregations.asMap() : null;
                    ParsedDateHistogram dateHistogram =  aggMap != null ? (ParsedDateHistogram)aggMap.get("date") : null;
                    List<? extends Histogram.Bucket> buckets = dateHistogram != null ? dateHistogram.getBuckets() : null;
                    if (buckets != null) {
                        for (Histogram.Bucket bucket : buckets) {
                            String key  = bucket.getKeyAsString();
                            Cardinality cardinality  = bucket.getAggregations().get("dau");
                            long dau = cardinality.getValue();
                            if (key != null) {
                                Map<String, Object> map = new HashMap<>();
                                String date = key.substring(0, 10);
                                map.put("indexName", "goose_daily_active_user");
                                map.put("id", productId + "-" + date);
                                map.put("date", date);
                                map.put("dailyActiveUser", dau);
                                map.put("productId", productId);
                                map.put("releaseId", releaseId);
                                map.put("playActionIndex", index);
                                map.put("LocalCreateTime", new Date());
                                billUpdateList.add(map);
                                logger.info("added a DAU: {}", map);
                            }
                        }
                    }
                    if (billUpdateList.size() > 0 ) {
                        BulkRequest bulkRequest = new BulkRequest();
                        for (Map childMap : billUpdateList) {
                            UpdateRequest updateRequest = new UpdateRequest((String) childMap.remove("indexName"), "_doc", childMap.remove("id").toString());
                            updateRequest.doc(childMap).upsert(childMap);
                            bulkRequest.add(updateRequest);
                        }
                        queryService.updateEsData(bulkRequest);
                        logger.info("update goose_active_user * size:{}", billUpdateList.size());
                        billUpdateList.clear();
                    }
                } catch (Exception e) {
                    logger.error("execute error:", e);
                }
            }
        } catch (Exception e) {
            logger.error("execute error:",  e);
            workItem.setResult("false");
            workItem.setEndTime(new Date(System.currentTimeMillis()));
            workItem.setErrorMsg(e.getMessage());
            return workItem;
        }
        workItem.setResult("true");
        workItem.setEndTime(new Date(System.currentTimeMillis()));
        workItem.setTer(endDate);
        return workItem;
    }
}
