package com.vms.metric.analyse.service.job.live;

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
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 往 account_charge_log_mix 里添加login_id字段，login_id字段在goose割接前后分别来自 account_service tve_account_info 两个索引
 */
@Component
public class ChargeLogLoginIdAnalyse extends ElsDocumentStreamScanning {
    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private List<Map> billUpdateList;
    private String[] account_service_indices = new String[] {"account_service_mix", "tve_account_info_mix"};


    @Autowired
    public ChargeLogLoginIdAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
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
        setting.setIndex(new String[]{"account_charge_log_mix"});
        setting.setFieldName("create_time");
        setting.setSortFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.OVERLAPPED_INCREMENTAL);
        setting.setOverlappedDays(3);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolQuery.must(QueryBuilders.existsQuery("spider_source"));

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
                try {
                    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                    long account_id = v.get("account_id") != null ? ((Number)v.get("account_id")).longValue() : ((Number)v.get("charge_account_id")).longValue();
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.should(QueryBuilders.matchQuery("account_id", account_id));
                    boolQuery.should(QueryBuilders.matchQuery("charge_account_id", account_id));
                    sourceBuilder.query(boolQuery);
                    sourceBuilder.size(2000);
                    //获得对应的account_service  / tve_account_info 表里的 login_id
                    for (String account_service_index : account_service_indices) {
                        List<Map<String, Object>> list = queryService.queryEsDataAsList(sourceBuilder, account_service_index);
                        if (list != null && list.size() > 0) {
                            Map<String, Object> account_service = list.get(0);
                            Map<String, Object> toUpdate = new HashMap<>();
                            toUpdate.put("indexName", account_charge_log_index);
                            toUpdate.put("id", id);
                            if ("account_service_mix".equals(account_service_index))
                                toUpdate.put("login_id", account_service.get("login_id"));
                            else if ("tve_account_info_mix".equals(account_service_index))
                                toUpdate.put("login_id", account_service.get("account_name"));
                            billUpdateList.add(toUpdate);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    logger.error("error", ex);
                }

        });
    }


}
