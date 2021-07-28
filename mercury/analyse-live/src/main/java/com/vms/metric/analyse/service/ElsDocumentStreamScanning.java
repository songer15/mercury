package com.vms.metric.analyse.service;

import com.vms.metric.analyse.model.ElsIndexConfig;
import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.model.ElsQuerySetting;
import com.vms.metric.analyse.tool.CustomTool;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Gavin.hu
 * 2019/3/15
 * 遍历某项数据
 **/
public abstract class ElsDocumentStreamScanning implements BaseDataAnalyse {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Calendar calendar = Calendar.getInstance();

    public abstract void initSampleData() throws Exception;

    public abstract Tuple<ElsAnalyseESQueryService, ElsQuerySetting> initStreaming(Date date, WorkItem workItem) throws Exception;

    public abstract void batchScanningFinish() throws Exception;

    public abstract void iterator(List<Map<String, Object>> dataList);

    @Override
    public WorkItem execute(WorkItem workItem) {
        workItem.setStartTime(new Date());
        Date startTime;
        Date endTime;
        try {
            initSampleData();
            //init config
            String pre = workItem.getPre();
            Date preTime;
            if (pre != null && pre.contains(":"))
                preTime = sdf.parse(workItem.getPre());
            else if (pre != null)
                preTime = new Date(Long.parseLong(pre));
            else throw new Exception("null pre");

            Tuple<ElsAnalyseESQueryService, ElsQuerySetting> tuple = initStreaming(preTime, workItem);
            ElsAnalyseESQueryService queryService = tuple.v1();
            ElsQuerySetting setting = tuple.v2();

            //setting query time range
            ElsQuerySetting.ScanStrategy scanStrategy = setting.getScanStrategy();
            switch (scanStrategy) {
                case FULL:
                    startTime = new Date(0);
                    endTime = new Date(System.currentTimeMillis());
                    break;
                case DAYTIMEZORO:
                    calendar.setTime(preTime);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTime = calendar.getTime();
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                    calendar.set(Calendar.SECOND, 1);
                    endTime = calendar.getTime();
                    break;
                case INCREMENTAL:
                    startTime = preTime;
                    endTime = new Date();
                    break;
                case CUSTOM:
                    Tuple<Date, Date> startAndEndTime = setting.getTimeRange();
                    startTime = startAndEndTime.v1();
                    endTime = startAndEndTime.v2();
                    break;
                case OVERLAPPED_INCREMENTAL:
                    long preMilis = preTime.getTime() - setting.getOverlappedDays() * 24 * 60 * 60 * 1000L;
                    if (preMilis < 0)
                        startTime = new Date(0);
                    else
                        startTime = new Date(preMilis);
                    endTime = new Date();
                    break;
                default:
                    throw new Exception("scanStrategy error");
            }

            //start
            QueryBuilder rangeQuery = QueryBuilders
                    .rangeQuery(setting.getFieldName())
                    .from(startTime)
                    .to(endTime).timeZone("Asia/Shanghai");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            if (setting.getQueryBuilders() != null) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(rangeQuery);
                for (QueryBuilder queryBuilder : setting.getQueryBuilders())
                    boolQueryBuilder.must(queryBuilder);
                sourceBuilder.query(boolQueryBuilder);
            } else
                sourceBuilder.query(rangeQuery);
            sourceBuilder.timeout(TimeValue.timeValueMillis(5 * 60_000));
            sourceBuilder.size(10000);
            if (setting.getIncludeFields() != null && setting.getIncludeFields().length != 0)
                sourceBuilder.fetchSource(setting.getIncludeFields(), new String[]{});
            if (setting.getSortFieldName() != null && setting.getSortOrder() != null)
                sourceBuilder.sort(new FieldSortBuilder(setting.getSortFieldName()).order(setting.getSortOrder()));
            Map dataMap = queryService.queryEsData(sourceBuilder, setting.getIndex());
            List<Map<String, Object>> dataList;
            while (true) {
                String scrollId = (String) dataMap.get("scroll_id");
                if (StringUtils.isEmpty(scrollId))
                    break;
                dataList = (List<Map<String, Object>>) dataMap.get("data");
                if (dataList.size() == 0)
                    break;

                iterator(dataList);

                dataList.clear();
                dataMap.clear();
                dataMap = queryService.queryEsDataByScrollId(scrollId);
            }
            batchScanningFinish();
        } catch (IOException e) {
            //connect es fail
            logger.error("{} connect es fail:{}", this.getClass().getName(), e);
            workItem.setResult("false");
            workItem.setEndTime(new Date(System.currentTimeMillis()));
            workItem.setErrorMsg(e.getMessage());
            return workItem;
        } catch (Exception e) {
            logger.error("{} execute error:{}", this.getClass().getName(), e);
            workItem.setResult("false");
            workItem.setEndTime(new Date(System.currentTimeMillis()));
            workItem.setErrorMsg(e.getMessage());
            return workItem;
        }
        workItem.setResult("true");
        workItem.setEndTime(new Date(System.currentTimeMillis()));
        workItem.setTer(sdf.format(endTime));
        return workItem;
    }

    protected int writeToEls(List<Map> writeList, ElsIndexConfig config, ElsAnalyseESWriterService writerService) throws Exception {
        int successNums = 0;
        if (writeList.size() > 2000) {
            List<List<Map>> subLists = CustomTool.splitList(writeList, 2000);
            for (List<Map> childWriteList : subLists) {
                int successNum = writerService.write(config, childWriteList);
                if (successNum != 0)
                    logger.info("writeToEls success");
                else {
                    logger.info("writeToEls fail");
                    throw new Exception("writeToEls fail");
                }
                successNums += successNum;
            }
        } else if (writeList.size() > 0) {
            if ((successNums = writerService.write(config, writeList)) != 0)
                logger.info("writeToEls success");
            else {
                logger.info("writeToEls fail");
                throw new Exception("writeToEls fail");
            }
        }
        return successNums;
    }

    protected void updateEsData(ElsAnalyseESQueryService queryService, List<UpdateRequest> updateRequestList) throws Exception {
        if (updateRequestList.size() > 2000) {
            List<List<UpdateRequest>> subLists = CustomTool.splitList(updateRequestList, 2000);
            for (List<UpdateRequest> childWriteList : subLists) {
                if (queryService.updateEsData(childWriteList))
                    logger.info("update elasticsearch data success");
                else {
                    logger.info("update elasticsearch data has fail");
//                    throw new Exception("update elasticsearch data fail");
                }
            }
        } else if (updateRequestList.size() > 0) {
            if (queryService.updateEsData(updateRequestList))
                logger.info("update elasticsearch data success");
            else {
                logger.info("update elasticsearch data has fail");
//                throw new Exception("update elasticsearch data fail");
            }
        }
    }
}
