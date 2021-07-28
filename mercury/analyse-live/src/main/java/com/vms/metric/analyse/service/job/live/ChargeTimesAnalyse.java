package com.vms.metric.analyse.service.job.live;

import com.google.common.collect.Sets;
import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.ElsAnalyseESQueryService;
import com.vms.metric.analyse.service.ElsAnalyseESWriterService;
import com.vms.metric.analyse.service.ElsDocumentStreamScanning;
import com.vms.metric.analyse.model.ElsQuerySetting;
import com.vms.metric.analyse.tool.CustomTool;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 从 account_charge_log* 里判断account_id是第几次充值
 */
@Component
public class ChargeTimesAnalyse extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private List<Map> billUpdateList;
    private Set<String> tve = Sets.newHashSet("tve_web", "mobile_tveweb", "web");
    private Set<String> red = Sets.newHashSet("redplaybox", "rpm", "web");
    private Set<String> blue = Sets.newHashSet("bluetv", "pmbluetv",  "web");

    @Autowired
    public ChargeTimesAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
        this.queryService = queryService;
        this.writerService = writerService;
        billUpdateList = new ArrayList<>();
    }

    @Override
    public void initSampleData() throws Exception {
        billUpdateList.clear();
    }

    @Override
    public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception {
        ElsQuerySetting setting = new ElsQuerySetting();
        setting.setIndex(new String[]{ "account_charge_log_redplay_mobile", "account_charge_log_bluetv" , "tve_account_charge_log"});
        setting.setFieldName("create_time");
        setting.setSortFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.OVERLAPPED_INCREMENTAL);
        setting.setOverlappedDays(3);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "web"));
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "tve_web"));
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "mobile_tveweb"));
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "redplaybox"));
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "rpm"));
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "bluetv"));
        boolQuery.should().add(QueryBuilders.matchQuery("charge_release_id", "pmbluetv"));

        setting.setQueryBuilders(new QueryBuilder[]{boolQuery});
        setting.setSortOrder(SortOrder.ASC);
        return new Tuple<>(queryService, setting);
    }

    @Override
    public void batchScanningFinish() throws Exception {

        if (billUpdateList.size() > 2000) {
            List<List<Map>> subLists = CustomTool.splitList(billUpdateList, 2000);
            for (List<Map> list : subLists) {
                BulkRequest bulkRequest = new BulkRequest();
                for (Map childMap : list) {
                    UpdateRequest updateRequest = new UpdateRequest((String) childMap.remove("indexName"), "_doc", childMap.remove("id").toString());
                    updateRequest.doc(childMap).upsert(childMap);
                    bulkRequest.add(updateRequest);
                }
                queryService.updateEsData(bulkRequest);
            }
        } else if (billUpdateList.size() > 0) {
            BulkRequest bulkRequest = new BulkRequest();
            for (Map childMap : billUpdateList) {
                UpdateRequest updateRequest = new UpdateRequest((String) childMap.remove("indexName"), "_doc", childMap.remove("id").toString());
                updateRequest.doc(childMap).upsert(childMap);
                bulkRequest.add(updateRequest);
            }
            queryService.updateEsData(bulkRequest);
        }
        logger.info("update account_charge_log * size:{}", billUpdateList.size());
        billUpdateList.clear();
    }


    @Override
    public void iterator(List<Map<String, Object>> dataList) {
        dataList.forEach(v -> {
            String id = (String)v.get("_id");
            String account_charge_log_index = (String) v.get("_indexName");
            String charge_release_id = (String) v.get("charge_release_id");
            Set<String> possibleReleaseId = unifyReleaseId(charge_release_id, account_charge_log_index);
            if (possibleReleaseId != null) {
                    try {
                        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                        if(v.get("category") != null) {
                            String duid = (String)v.get("device_unique_id");
                            boolQueryBuilder.must(QueryBuilders.matchQuery("device_unique_id", duid));
                        } else {
                            long account_id = v.get("account_id") != null ? ((Number)v.get("account_id")).longValue() : ((Number)v.get("charge_account_id")).longValue();
                            BoolQueryBuilder should = QueryBuilders.boolQuery();
                            should.should(QueryBuilders.matchQuery("account_id", account_id));
                            should.should(QueryBuilders.matchQuery("charge_account_id", account_id));
                            boolQueryBuilder.must(should);
                        }
                        boolQueryBuilder.must(QueryBuilders.termsQuery("charge_release_id", possibleReleaseId));
                        sourceBuilder.size(2000);
                        sourceBuilder.query(boolQueryBuilder);
                        sourceBuilder.sort((new FieldSortBuilder("charge_ts").order(SortOrder.ASC)));
                        List<Map<String, Object>> list = queryService.queryEsDataAsList(sourceBuilder, account_charge_log_index);
                        //logger.info("id: {}, list size: {}", id, list.size());
                        AtomicInteger count = new AtomicInteger(1);
                        for (Map<String, Object> account_charge_log: list) {
                            //在charge_ts升序的充值记录里找到该记录
                            if (id.equals(account_charge_log.get("_id"))) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("indexName", account_charge_log_index);
                                map.put("id", id);
                                map.put("charge_times", count.get());
                                billUpdateList.add(map);
                                break;
                            }
                            count.getAndIncrement();
                        }
                    } catch (Exception ex) {
                        logger.error("error", ex);
                    }
            }
        });
    }

    private Set<String> unifyReleaseId(String releaseId, String index) {
        if (tve.contains(releaseId) && "tve_account_charge_log".equals(index))
            return tve;
        if (red.contains(releaseId)  && "account_charge_log_redplay_mobile".equals(index))
            return red;
        if (blue.contains(releaseId) && "account_charge_log_bluetv".equals(index))
            return blue;
        return null;
    }
}
