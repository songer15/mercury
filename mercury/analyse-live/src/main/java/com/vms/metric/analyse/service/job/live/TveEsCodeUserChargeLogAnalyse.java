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
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TveEsCodeUserChargeLogAnalyse extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private List<Map> billUpdateList;

    @Autowired
    public TveEsCodeUserChargeLogAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
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
        setting.setIndex(new String[]{"account_service_mix", "tve_account_info_mix"});
        setting.setFieldName("create_time");
        setting.setSortFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.OVERLAPPED_INCREMENTAL);
        setting.setOverlappedDays(3);
//        setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.matchQuery("charge_code_first_login", 1)});
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
            String indexName = (String) v.get("_indexName");
            Map<String, Object> map = new HashMap<>();
            //旧的 account_service
            if (indexName.equals("account_service_mix") && 5 == (int)v.get("login_method")) {
                Long account_id = ((Number)v.get("account_id")).longValue();
                String duid = (String) v.get("first_login_device");
                map.put("indexName", "account_charge_log_mix");
                map.put("id",  v.get("login_id"));
                map.put("device_unique_id", duid);
                map.put("account_id", account_id);
                map.put("release_id", v.get("release_id"));
                map.put("login_method", v.get("login_method"));
                map.put("login_id", v.get("login_id"));
                map.put("charge_ts", v.get("create_time"));
                map.put("create_time", v.get("create_time"));
                map.put("expire_ts", v.get("expire_ts"));
                map.put("charge_release_id", v.get("register_release_id"));
                map.put("card_value", v.get("period"));
                map.put("category", "codeUser");
            } //新的 account_service
              else if (indexName.equals("tve_account_info_mix") && v.get("charge_code_first_login") != null && ((Number)v.get("charge_code_first_login")).intValue() == 1) {
                Long account_id = ((Number)v.get("account_id")).longValue();
                String duid = ((Integer) v.get("first_login_device")).toString();
                map.put("indexName", "account_charge_log_mix");
                map.put("id",  v.get("account_name"));
                map.put("device_unique_id", duid);
                map.put("account_id", account_id);
                map.put("login_id", v.get("account_name"));
                map.put("charge_ts", v.get("create_time"));
                map.put("create_time", v.get("create_time"));
                map.put("expire_ts", v.get("expire_ts"));
                map.put("charge_release_id", v.get("first_login_app"));
                map.put("card_value", v.get("period"));
                map.put("category", "codeUser");
            }

            if (map.get("id") != null && map.get("indexName") != null)
                billUpdateList.add(map);
        });
    }
}
