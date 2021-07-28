package com.vms.metric.analyse.service;

import com.vms.metric.analyse.model.ElsQuerySetting;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gavin.hu
 * 2019/3/15
 * 遍历某项数据
 **/
public abstract class ElsDocumentBatchScanning {

    public abstract Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initBatch();

    public List<Map<String, Object>> query() throws Exception {
        //init config
        Tuple<ElsAnalyseESQueryService, ElsQuerySetting> tuple = initBatch();
        ElsAnalyseESQueryService queryService = tuple.v1();
        ElsQuerySetting setting = tuple.v2();

        //start
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if (setting.getQueryBuilders() == null) throw new Exception("null QueryBuilders");
        else if (setting.getQueryBuilders().length == 1)
            sourceBuilder.query(setting.getQueryBuilders()[0]);
        else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (QueryBuilder queryBuilder : setting.getQueryBuilders())
                boolQueryBuilder.must(queryBuilder);
            sourceBuilder.query(boolQueryBuilder);
        }
        sourceBuilder.timeout(TimeValue.timeValueMillis(5 * 60_000));
        sourceBuilder.size(2000);
        if (setting.getIncludeFields() != null && setting.getIncludeFields().length != 0)
            sourceBuilder.fetchSource(setting.getIncludeFields(), new String[]{});
        if (setting.getSortFieldName() != null && setting.getSortOrder() != null)
            sourceBuilder.sort(new FieldSortBuilder(setting.getSortFieldName()).order(setting.getSortOrder()));
        Map dataMap = queryService.queryEsData(sourceBuilder, setting.getIndex());
        List<Map<String, Object>> dataList;
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (true) {
            String scrollId = (String) dataMap.get("scroll_id");
            if (StringUtils.isEmpty(scrollId))
                break;
            dataList = (List<Map<String, Object>>) dataMap.get("data");
            if (dataList.size() == 0)
                break;

            resultList.addAll(dataList);

            dataList.clear();
            dataMap.clear();
            dataMap = queryService.queryEsDataByScrollId(scrollId);
        }
        return resultList;
    }

}
