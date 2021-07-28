package com.vms.metric.analyse.service.job.live;

import com.vms.metric.analyse.model.ElsQuerySetting;
import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.ElsAnalyseESQueryService;
import com.vms.metric.analyse.service.ElsAnalyseESWriterService;
import com.vms.metric.analyse.service.ElsDocumentBatchScanning;
import com.vms.metric.analyse.service.ElsDocumentStreamScanning;
import com.vms.metric.analyse.tool.CustomTool;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class TveChargeDeviceAnalyse extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private List<Map> billUpdateList = new ArrayList<>();
    private Map<Integer, String> modelUsers = new HashMap<>();

    @Autowired
    public TveChargeDeviceAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
        this.queryService = queryService;
        this.writerService = writerService;

    }

    @Override
    public void initSampleData() throws Exception {
        modelUsers.clear();
        billUpdateList.clear();
        new ElsDocumentBatchScanning() {
            @Override
            public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initBatch() {
                ElsQuerySetting setting = new ElsQuerySetting();
                setting.setIndex(new String[]{"tve_account_info_tve"});
                //筛选出充值码登录的用户
                setting.setQueryBuilders(new QueryBuilder[]{
                        QueryBuilders.existsQuery("account_id"),
                        QueryBuilders.existsQuery("first_login_device_type")});
                setting.setIncludeFields(new String[]{"account_id", "first_login_device_type"});
                return new Tuple<>(queryService, setting);
            }
            //
        }.query().stream().forEach(v -> {
            modelUsers.put(((Number)v.get("account_id")).intValue(), (String)v.get("first_login_device_type"));
        });
        logger.info("model user size : {}", modelUsers.size());
    }

    @Override
    public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception {
        ElsQuerySetting setting = new ElsQuerySetting();
        setting.setIndex(new String[]{"tve_account_charge_log"});

        setting.setFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.INCREMENTAL);

        BoolQueryBuilder shouldQueryBuilder = QueryBuilders.boolQuery();
        //可能是account_id或者 charge_account_id
        shouldQueryBuilder.should(QueryBuilders.existsQuery("account_id"));  //tve
        shouldQueryBuilder.should(QueryBuilders.existsQuery("charge_account_id"));  //bluetv

        setting.setQueryBuilders(new QueryBuilder[]{shouldQueryBuilder});
        setting.setIncludeFields(new String[]{"_id", "charge_account_id","account_id"});
        return new Tuple<>(queryService, setting);
    }

    @Override
    public void batchScanningFinish() throws Exception {

        if (billUpdateList.size() > 2000) {
            List<List<Map>> subLists = CustomTool.splitList(billUpdateList, 2000);
            for (List<Map> list : subLists) {
                BulkRequest bulkRequest = new BulkRequest();
                for (Map childMap : list) {
                    if (childMap != null) {
                        UpdateRequest updateRequest = new UpdateRequest("tve_account_charge_log", "_doc", (String)childMap.remove("id"));
                        //这里采用upsert的方式，而不是仅仅更新之前的document
                        updateRequest.doc(childMap).upsert(childMap);
                        bulkRequest.add(updateRequest);
                    }
                }
                queryService.updateEsData(bulkRequest);
            }
        } else if (billUpdateList.size() > 0) {
            BulkRequest bulkRequest = new BulkRequest();
            for (Map childMap : billUpdateList) {
                UpdateRequest updateRequest = new UpdateRequest("tve_account_charge_log", "_doc", childMap.remove("id").toString());
                updateRequest.doc(childMap).upsert(childMap);
                bulkRequest.add(updateRequest);
            }
            queryService.updateEsData(bulkRequest);
        }
        logger.info("update  tve_account_charge_log success:{}", billUpdateList.size());
    }

    @Override
    public void iterator(List<Map<String, Object>> dataList) {
        dataList.forEach(v -> {
            String _id = (String) v.get("_id");
            Integer account_id = v.get("account_id") != null ?  ((Number)v.get("account_id")).intValue() : ((Number)v.get("charge_account_id")).intValue();
            if (modelUsers.containsKey(account_id)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", _id);
                map.put("first_login_device_type", modelUsers.get(account_id));
                billUpdateList.add(map);
            }
        });
        logger.info("billUpdateList  size:{}", billUpdateList.size());
    }
}
