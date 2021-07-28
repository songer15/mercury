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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 查询  tve_account_info_*, 往 account_charge_log_* 添加 login_method 字段，
 */
@Component
public class LoginMethodChargeLogAnalyse extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private List<Map> billUpdateList;

    @Autowired
    public LoginMethodChargeLogAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
        this.queryService = queryService;
        this.writerService = writerService;
        billUpdateList = new ArrayList<>();
    }

    @Override
    public void initSampleData() throws Exception {

    }

    @Override
    public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception {
        ElsQuerySetting setting = new ElsQuerySetting();
        setting.setIndex(new String[]{"tve_account_charge_log", "account_charge_log_bluetv", "account_charge_log_mix", "account_charge_log_redplay_mobile"});
        setting.setFieldName("create_time");
        setting.setSortFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.OVERLAPPED_INCREMENTAL);
        setting.setOverlappedDays(3);
        setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("login_method"))});
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
        logger.info("update _account_charge_log * size:{}", billUpdateList.size());
        billUpdateList.clear();
    }

    @Override
    public void iterator(List<Map<String, Object>> dataList) {
        dataList.forEach(v -> {
            long account_id = v.get("account_id") != null ? ((Number)v.get("account_id")).longValue() : ((Number)v.get("charge_account_id")).longValue();
            String account_charge_log_index = (String) v.get("_indexName");
            String id = (String)v.get("_id");
            //去对应的account_service里找login_method
            String tve_account_info_index = convertIndexName(account_charge_log_index);
            if (tve_account_info_index != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("indexName", account_charge_log_index);
                try {
                    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                    sourceBuilder.query(QueryBuilders.matchQuery("account_id", account_id));
                    sourceBuilder.size(2000);
                    List<Map<String, Object>> list = queryService.queryEsDataAsList(sourceBuilder, tve_account_info_index);
                    for (Map<String, Object> account_service: list) {
                        map.put("login_method", account_service.get("login_method"));
                        map.put("id", id);
                        billUpdateList.add(map);
                        break;
                    }
                } catch (Exception ex) {
                    logger.error("error", ex);
                }
            }
        });
    }

    /**
     * 根据release_id 找到对应的 account_charge_log 索引名
     */
    private String convertIndexName(String name) {
        if ("tve_account_charge_log".equals(name)) {
            return "tve_account_info_tve";
        }
        if ("account_charge_log_bluetv".equals(name)) {
            return "tve_account_info_bluetv";
        }
        if ("account_charge_log_redplay_mobile".equals(name)) {
            return "tve_account_info_red";
        }
        if ("account_charge_log_mix".equals(name)) {
            return "tve_account_info_mix";
        }
        return null;
    }

}
