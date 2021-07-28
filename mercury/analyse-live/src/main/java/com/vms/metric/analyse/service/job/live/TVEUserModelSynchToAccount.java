package com.vms.metric.analyse.service.job.live;

import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.ElsAnalyseESQueryService;
import com.vms.metric.analyse.service.ElsDocumentStreamScanning;
import com.vms.metric.analyse.model.ElsQuerySetting;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Gavin.hu
 * 2019/6/17
 * 添加所某渠道预装用户中激活的信息，根据已有mac增加对应的duid 和 activeTime,如果是老账户未升级新版本，还要添加email账户信息）,。激活数据来自 tve_account_free_trial
 **/
@Component
public class TVEUserModelSynchToAccount extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    //不存在duid(未激活)的mac集合
    private Set<String> akUsers = new HashSet<>();
    private List<UpdateRequest> updateList = new ArrayList<>();

    @Autowired
    public TVEUserModelSynchToAccount(ElsAnalyseESQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public void initSampleData() throws Exception {
        updateList.clear();
    }

    @Override
    public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception {
        ElsQuerySetting setting = new ElsQuerySetting();
        setting.setIndex(new String[]{"tve_account_free_trial_tve"});
        setting.setFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.INCREMENTAL);
        setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.existsQuery("device_id"), QueryBuilders.existsQuery("mac")});
        setting.setIncludeFields(new String[]{"device_id", "activation_ts", "create_time","expire_ts", "mac"});
        return new Tuple<>(queryService, setting);
    }

    @Override
    public void batchScanningFinish() throws Exception {
        logger.info("update list size {}", updateList.size());
        updateEsData(queryService, updateList);
        updateList.clear();
    }

    @Override
    public void iterator(List<Map<String, Object>> dataList) {
        dataList.forEach(v -> {
            String mac = (String) v.get("mac"); // user_probation_tve里的did和 tve_akwd_user里的mac对应
            if (!StringUtils.isEmpty(mac)) {
                //艾科渠道的盒子
                String device_id = v.get("device_id").toString();
                UpdateRequest updateRequest = new UpdateRequest("tve_akwd_user", "_doc", mac);
                Map<String, Object> map = new HashMap<>();
                map.put("duid", device_id);
                map.put("activeTime", v.get("activation_ts") == null ? null : new Date(((Number) v.get("activation_ts")).longValue()));
                if (device_id != null && device_id.contains("@")) {
                    //老账户未升级新版本
                    map.put("email", device_id);
                    map.put("emailRegisterTime", v.get("create_time") == null ? null : new Date(((Number) v.get("create_time")).longValue()));
                    map.put("emailExpireTime", v.get("expire_ts") == null ? null : new Date(((Number) v.get("expire_ts")).longValue()));
                }
                updateRequest.doc(map);
                updateList.add(updateRequest);
            }
        });
    }
}
