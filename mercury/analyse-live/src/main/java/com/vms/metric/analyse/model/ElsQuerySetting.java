package com.vms.metric.analyse.model;

import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Date;

/**
 * @author Gavin.hu
 * 2019/3/25
 **/
public class ElsQuerySetting {
    private String[] index;
    private String fieldName;
    private String[] includeFields;
    private ScanStrategy scanStrategy;
    private Tuple<Date, Date> timeRange;
    private String sortFieldName;
    private SortOrder sortOrder;
    private QueryBuilder[] queryBuilders;
    private int overlappedDays;

    public QueryBuilder[] getQueryBuilders() {
        return queryBuilders;
    }

    public void setQueryBuilders(QueryBuilder[] queryBuilders) {
        this.queryBuilders = queryBuilders;
    }

    public String getSortFieldName() {
        return sortFieldName;
    }

    public void setSortFieldName(String sortFieldName) {
        this.sortFieldName = sortFieldName;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) { this.sortOrder = sortOrder; }

    public Tuple<Date, Date> getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(Tuple<Date, Date> timeRange) {
        this.timeRange = timeRange;
    }

    public ScanStrategy getScanStrategy() {
        return scanStrategy;
    }

    public void setScanStrategy(ScanStrategy scanStrategy) {
        this.scanStrategy = scanStrategy;
    }

    public String[] getIndex() {
        return index;
    }

    public void setIndex(String[] index) {
        this.index = index;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String[] getIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(String[] includeFields) {
        this.includeFields = includeFields;
    }

    public int getOverlappedDays() {
        return overlappedDays;
    }

    public void setOverlappedDays(int overlappedDays) {
        this.overlappedDays = overlappedDays;
    }

    public enum ScanStrategy {
        FULL, INCREMENTAL, DAYTIMEZORO, CUSTOM, OVERLAPPED_INCREMENTAL
    }
}

