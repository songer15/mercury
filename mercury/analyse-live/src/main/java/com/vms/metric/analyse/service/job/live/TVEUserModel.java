package com.vms.metric.analyse.service.job.live;

import com.vms.metric.analyse.model.ElsIndexConfig;
import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.ElsAnalyseESQueryService;
import com.vms.metric.analyse.service.ElsAnalyseESWriterService;
import com.vms.metric.analyse.service.ElsDocumentStreamScanning;
import com.vms.metric.analyse.model.ElsQuerySetting;
import com.vms.metric.analyse.tool.CustomTool;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Gavin.hu
 * 2019/6/17
 * 得到某渠道预装用户
 **/
@Component
public class TVEUserModel extends ElsDocumentStreamScanning {

    private final ElsAnalyseESQueryService queryService;
    private final ElsAnalyseESWriterService writerService;
    private ElsIndexConfig config;
    private List<Map> writeList = new ArrayList<>();

    @Autowired
    public TVEUserModel(ElsAnalyseESQueryService queryService, ElsAnalyseESWriterService writerService) {
        this.queryService = queryService;
        this.writerService = writerService;
        config = new ElsIndexConfig();
        config.setIndexName("tve_akwd_user");
        config.setNeedTime(true);
        config.setIdField("mac");
        config.setNeedGeo(false);
        config.setMappingFile("mapping/tve_akwd_user.json");
    }

    @Override
    public void initSampleData() throws Exception {
        writeList.clear();
    }

    @Override
    public Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception {
        ElsQuerySetting setting = new ElsQuerySetting();
        setting.setIndex(new String[]{"mfc_user_info-2018*"});

        setting.setFieldName("create_time");
        setting.setScanStrategy(ElsQuerySetting.ScanStrategy.INCREMENTAL);
        setting.setQueryBuilders(new QueryBuilder[]{QueryBuilders.matchAllQuery()});
        setting.setIncludeFields(new String[]{"did", "batch_number", "create_time", "vendor_id", "reg_date"});
        return new Tuple<>(queryService, setting);
    }

    @Override
    public void batchScanningFinish() throws Exception {
        int num = writeToEls(writeList, config, writerService);
        logger.info("add new ak user success:{}", num);
        writeList.clear();
    }

    @Override
    public void iterator(List<Map<String, Object>> dataList) {
        dataList.forEach(v -> {
            String did = (String) v.get("did");
            Map<String, Object> map = new HashMap<>();
            map.put("mac", did);
            map.put("createTime", CustomTool.transStrToDate((String) v.get("create_time")));
            map.put("batchNumber", v.get("batch_number"));
            map.put("vendorId", v.get("vendor_id"));
            writeList.add(map);
        });
    }
}
