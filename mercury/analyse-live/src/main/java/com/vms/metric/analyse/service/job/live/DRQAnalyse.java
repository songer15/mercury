package com.vms.metric.analyse.service.job.live;

import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.ElsAnalyseESQueryService;
import com.vms.metric.analyse.service.BaseDataAnalyse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.metrics.valuecount.ParsedValueCount;
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
 * 统计日充值用户
 */

@Component
public class DRQAnalyse implements BaseDataAnalyse {
    @Autowired
    private ElsAnalyseESQueryService queryService;
    protected static Logger logger = LoggerFactory.getLogger(DRQAnalyse.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private List<Map> billUpdateList  = new ArrayList<>();

    @Override
    public WorkItem execute(WorkItem workItem)  {
        workItem.setStartTime(new Date());
        //一次计算1天的充值
        String startDate = workItem.getPre();
        Asserts.notNull(startDate, "startDate is null");
        String endDate = sdf.format(new Date());
        Asserts.notNull(endDate, "endDate is null");
        try {
            List<String> products = new ArrayList<>();
            try {
                FileReader fr = new FileReader("cfg/drq.txt");
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
                    String productIds = productInfo[0];
                    String chargeReleaseIds = productInfo[1];
                    String index = productInfo[2];
                    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                    logger.info("executing drq task: {}", s);
                    //按天聚合 count
                    AggregationBuilder aggregationBuilder = AggregationBuilders
                            .dateHistogram("date")
                            .field("create_time")
                            .interval(1000 * 60 * 60 * 24)
                            .timeZone(DateTimeZone.forOffsetHours(8))
                            .minDocCount(1)
                            .subAggregation(
                                    AggregationBuilders
                                            .count("drq")
                                            .field("create_time"));

                    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder web = new BoolQueryBuilder();
                    //可以是web渠道充值
                    web.must(QueryBuilders.matchPhraseQuery("charge_release_id", "web"));
                    web.must(QueryBuilders.matchPhraseQuery("charge_type", "ADD"));
                    boolQueryBuilder.should(web);
                    //可以是手机/盒子渠道充值
                    for (String chargeReleaseId : chargeReleaseIds.split(",")) {
                        boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("charge_release_id", chargeReleaseId));
                    }
                    QueryBuilder queryBuilder =  QueryBuilders
                            .boolQuery()
                            .must(QueryBuilders
                                    .rangeQuery("create_time")
                                    .timeZone("Asia/Shanghai")
                                    .gte(startDate)
                                    .lte(endDate)
                                    .format("strict_date"))
                            .must(boolQueryBuilder);
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
                            ParsedValueCount count  = bucket.getAggregations().get("drq");
                            long drq = count.getValue();
                            if (key != null) {
                                for (String productId: productIds.split(",")) {
                                    Map<String, Object> map = new HashMap<>();
                                    String date = key.substring(0, 10);
                                    map.put("indexName", "goose_daily_recharge_quantity");
                                    map.put("id", productId + "-" + date);
                                    map.put("date", date);
                                    map.put("dailyRechargeQuantity", drq);
                                    map.put("productId", productId);
                                    map.put("chargeReleaseId", chargeReleaseIds);
                                    map.put("accountChargeLogIndex", index);
                                    map.put("LocalCreateTime", new Date());
                                    billUpdateList.add(map);
                                    logger.info("added a DRQ: {}", map);
                                }
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
                        logger.info("update goose_daily_recharge_quantity * size:{}", billUpdateList.size());
                        billUpdateList.clear();
                    }
                } catch (Exception e) {
                    logger.error("execute error:", e);
                }
            }
        } catch (Exception e) {
            logger.error("execute error:", e);
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