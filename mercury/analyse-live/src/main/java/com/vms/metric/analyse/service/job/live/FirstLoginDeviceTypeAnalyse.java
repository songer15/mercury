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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 根据 tve_account_info 里的 first_login_mac，对照 tve_akwd_user查找出第一次登录的mac属于哪个产商（转化统计）
 */
@Component
public class FirstLoginDeviceTypeAnalyse extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private List<Map> billUpdateList = new ArrayList<>();
    private Map<String, Integer> vendorIdMap = new HashMap<>();

    @Autowired
    public FirstLoginDeviceTypeAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
        this.queryService = queryService;
        this.writerService = writerService;

    }

    @Override
    public void initSampleData() throws Exception {
        billUpdateList.clear();

        //从 tve_akwd_user里找到mac和 vendorId的对应关系
        new ElsDocumentBatchScanning() {
            @Override
            public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initBatch() {
                ElsQuerySetting setting = new ElsQuerySetting();
                setting.setIndex(new String[]{"tve_akwd_user"});
                setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.existsQuery("vendorId")});
                setting.setIncludeFields(new String[]{"_id", "vendorId"});
                return new Tuple<>(queryService, setting);
            }
        }.query().stream().forEach(v -> {
            vendorIdMap.put((String) v.get("_id"), ((Number) v.get("vendorId")).intValue());
        });
        logger.info("vendorIdMap size:{}", vendorIdMap.size());
    }

    @Override
    public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception {
        ElsQuerySetting setting = new ElsQuerySetting();
        setting.setIndex(new String[]{"tve_account_info_tve"});
        setting.setFieldName("update_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.INCREMENTAL);
        setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.matchAllQuery()});
        setting.setIncludeFields(new String[]{"_id", "first_login_mac", "first_login_product_id"});
        return new Tuple<>(queryService, setting);
    }

    @Override
    public void batchScanningFinish() throws Exception {
        billUpdateList.clear();
    }

    @Override
    public void iterator(List<Map<String, Object>> dataList) {
        dataList.forEach(v -> {
            String _id =  (String) v.get("_id");
            String firstLoginMac = (String) v.get("first_login_mac");
            String productId =  (String) v.get("first_login_product_id");

            Map<String, Object> map = new HashMap<>();
            map.put("indexName", "tve_account_info_tve");
            map.put("id", _id);

            String first_login_device_type = null;
            if ("1000186".equals(productId)) {
                first_login_device_type = productId;
            }
            else if ("1000185".equals(productId)) {
                Integer vendorId  = vendorIdMap.get(firstLoginMac);
                if (vendorId != null) {
                    switch (vendorId) {
                        case 2000100: first_login_device_type = "akwd"; break;
                        case 2000500: first_login_device_type = "dzj"; break;
                        case 3001900: first_login_device_type = "txcz"; break;
                        case 3003000: first_login_device_type = "cyx"; break;
                        case 3003200: first_login_device_type = "hhkj"; break;
                    }
                } else {
                    first_login_device_type = "otherbox";
                }
            } else {
                first_login_device_type = "unknown";
            }
            map.put("first_login_device_type", first_login_device_type);
            billUpdateList.add(map);
        });

        if (billUpdateList.size() > 2000)
            update();
    }

    public void update() {
        try {
            if (billUpdateList.size() > 2000) {
                List<List<Map>> subLists = CustomTool.splitList(billUpdateList, 2000);
                for (List<Map> list : subLists) {
                    BulkRequest bulkRequest = new BulkRequest();
                    for (Map childMap : list) {
                        UpdateRequest updateRequest = new UpdateRequest(childMap.remove("indexName").toString(), "_doc", childMap.remove("id").toString());
                        //这里采用upsert的方式，而不是仅仅更新之前的document
                        updateRequest.doc(childMap).upsert(childMap);
                        bulkRequest.add(updateRequest);
                    }
                    queryService.updateEsData(bulkRequest);
                }
            } else if (billUpdateList.size() > 0) {
                BulkRequest bulkRequest = new BulkRequest();
                for (Map childMap : billUpdateList) {
                    UpdateRequest updateRequest = new UpdateRequest(childMap.remove("indexName").toString(), "_doc", childMap.remove("id").toString());
                    updateRequest.doc(childMap).upsert(childMap);
                    bulkRequest.add(updateRequest);
                }
                queryService.updateEsData(bulkRequest);
            }
            logger.info("update tve_account_info_tve success:{}", billUpdateList.size());
            billUpdateList.clear();
        } catch (Exception ex) {
            logger.error("update error", ex);
        }
    }
}
