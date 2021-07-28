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
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * 普通用户，通过login_id分组判断充值次数
 *  web充值用户，通过login_id分组判断充值次数
 *   code用户，通过duid判断充值次数
 */
@Component
public class TveEsChargeTimesAnalyse  extends ElsDocumentStreamScanning {
    private final ElsAnalyseESWriterService writerService;
    private final ElsAnalyseESQueryService queryService;
    private List<Map> billUpdateList;
    private String account_charge_log_index = "account_charge_log_mix";

    @Autowired
    public TveEsChargeTimesAnalyse(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
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
        setting.setIndex(new String[]{account_charge_log_index});
        setting.setFieldName("create_time");
        setting.setSortFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.OVERLAPPED_INCREMENTAL);
        setting.setOverlappedDays(3);
        setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.matchAllQuery()});
        setting.setSortOrder(SortOrder.ASC);
        return new Tuple<>(queryService, setting);
    }

    @Override
    public void batchScanningFinish() throws Exception {

        if (billUpdateList.size() > 500) {
            List<List<Map>> subLists = CustomTool.splitList(billUpdateList, 500);
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
       for (Map<String, Object> v : dataList) {
           String id = (String)v.get("_id");
           logger.info(v.toString());
           try {
               SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
               BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
               if(v.get("category") != null) {
                   //有部分为0的device_unique_id
                   if (v.get("device_unique_id") != null && !"0".equals(v.get("device_unique_id"))) {
                       BoolQueryBuilder should = QueryBuilders.boolQuery();
                       should.should(QueryBuilders.termQuery("device_unique_id", v.get("device_unique_id")));
                       should.should(QueryBuilders.termQuery("device_unique_id.keyword", v.get("device_unique_id")));
                       boolQueryBuilder.must(should);
                   }
                   else
                       continue;
               } else {
                   if (v.get("login_id") != null) {
                       BoolQueryBuilder should = QueryBuilders.boolQuery();
                       should.should(QueryBuilders.termQuery("login_id", v.get("login_id")));
                       should.should(QueryBuilders.termQuery("login_id.keyword", v.get("login_id")));
                       boolQueryBuilder.must(should);
                   } else {
                       Map<String, Object> map = new HashMap<>();
                       map.put("indexName", account_charge_log_index);
                       map.put("id", id);
                       map.put("charge_times", 1);
                       billUpdateList.add(map);
                       continue;
                   }
               }
               sourceBuilder.query(boolQueryBuilder);
               sourceBuilder.size(2000);
               sourceBuilder.sort((new FieldSortBuilder("charge_ts").order(SortOrder.ASC)));
               List<Map<String, Object>> list = queryService.queryEsDataAsList(sourceBuilder, account_charge_log_index);
               int count = 1;
               for (Map<String, Object> account_charge_log: list) {
                   //在charge_ts升序的充值记录里找到该记录
                   if (id.equals(account_charge_log.get("_id"))) {
                       Map<String, Object> map = new HashMap<>();
                       map.put("indexName", account_charge_log_index);
                       map.put("id", id);
                       map.put("charge_times", count);
                       billUpdateList.add(map);
                       break;
                   }
                   count++;
               }
               if (billUpdateList.size() > 500)
                   batchScanningFinish();
           } catch (Exception ex) {
               logger.error("tveEsChargeTimes error", ex);
           }
       }

       logger.info("process {} records", dataList.size());
    }

}
