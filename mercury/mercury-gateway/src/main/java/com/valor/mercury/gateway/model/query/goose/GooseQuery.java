package com.valor.mercury.gateway.model.query.goose;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GooseQuery {
    private static Map<String, GooseQuery> gooseQueries = new HashMap<>();
    private static final String DAILY_ACTIVE_USER = "dailyActiveUser";
    private static final String DAILY_RECHARGE_QUANTITY = "dailyRechargeQuantity";

    static {
        GooseQuery dailyActiveUser  = dailyActiveUser();
        gooseQueries.put(dailyActiveUser.getQueryId(), dailyActiveUser);

        GooseQuery dailyRechargeQuantity  = dailyRechargeQuantity();
        gooseQueries.put(dailyRechargeQuantity.getQueryId(), dailyRechargeQuantity);
    }

    private static GooseQuery dailyActiveUser() {
        GooseQuery gooseQuery = new GooseQuery();
        gooseQuery.queryId = "dailyActiveUser";
        gooseQuery.index = new String[]{"goose_daily_active_user"};
        gooseQuery.metricField = "dailyActiveUser";
        gooseQuery.timeField = "date";
        gooseQuery.timeRangeQueryBuilder = QueryBuilders.rangeQuery(gooseQuery.timeField);
        gooseQuery.sortBy = "date";
        return gooseQuery;
    }

    private static GooseQuery dailyRechargeQuantity() {
        GooseQuery gooseQuery = new GooseQuery();
        gooseQuery.queryId = "dailyRechargeQuantity";
        gooseQuery.index = new String[]{"goose_daily_recharge_quantity"};
        gooseQuery.metricField = "dailyRechargeQuantity";
        gooseQuery.timeField = "date";
        gooseQuery.timeRangeQueryBuilder = QueryBuilders.rangeQuery(gooseQuery.timeField);
        gooseQuery.sortBy = "date";
        return gooseQuery;
    }

    public static GooseQuery getByQueryId(String queryId) {
        switch (queryId) {
            case DAILY_ACTIVE_USER: return dailyActiveUser();
            case DAILY_RECHARGE_QUANTITY : return dailyRechargeQuantity();
            default: throw new RuntimeException(String.format("no such queryId: %s", queryId));
        }
    }

    private String queryId;
    private String[] index;
    private String[] type = new String[]{"_doc"};

    //时间过滤字段
    private String timeField;
    //时间过滤条件
    private RangeQueryBuilder timeRangeQueryBuilder;
    private boolean isStartInclusive = true;
    private boolean isEndInclusive = true;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String timeZone = "Asia/Shanghai";

    //排序
    private String sortBy;

    //统计指标字段名字
    private String metricField;
    //一般过滤条件
    private List<QueryBuilder> queryBuilders = new ArrayList<>();

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String[] getIndex() {
        return index;
    }

    public void setIndex(String[] index) {
        this.index = index;
    }

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String getMetricField() {
        return metricField;
    }

    public void setMetricField(String metricField) {
        this.metricField = metricField;
    }

    public void setTimeRange(String start, String end)  {
        timeRangeQueryBuilder.from(start, isStartInclusive).to(end, isEndInclusive).timeZone(timeZone);
    }

    public void addTermQuery(String fieldName, String fieldValue) {
        if (!StringUtils.isEmpty(fieldValue)) {
            queryBuilders.add(QueryBuilders.termQuery(fieldName, fieldValue));
        }
    }

    public SearchSourceBuilder getSearchSourceBuilder() {
        BoolQueryBuilder finalQueryBuilder = new BoolQueryBuilder();
        for (QueryBuilder queryBuilder : queryBuilders) {
            finalQueryBuilder.must(queryBuilder);
        }
        finalQueryBuilder.must(timeRangeQueryBuilder);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(finalQueryBuilder);
        searchSourceBuilder.sort(new FieldSortBuilder(sortBy).order(SortOrder.ASC));
        searchSourceBuilder.size(10000);
        return searchSourceBuilder;
    }

}
