package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexSearch {
    private long openContexts;
    private long queryTotal;
    private long queryTimeInMillis;
    private long queryCurrent;
    private long fetchTotal;
    private long fetchTimeInMillis;
    private long fetchCurrent;
    private long scrollTotal;
    private long scrollTimeInMillis;
    private long scrollCurrent;
    private long suggestTotal;
    private long suggestTimeInMillis;
    private long suggestCurrent;

    public static IndexSearch toIndexSearch(JsonObject jsonObject) {
        IndexSearch indexSearch = new IndexSearch();
        indexSearch.setOpenContexts(jsonObject.get("open_contexts").getAsLong());
        indexSearch.setQueryTotal(jsonObject.get("query_total").getAsLong());
        indexSearch.setQueryTimeInMillis(jsonObject.get("query_time_in_millis").getAsLong());
        indexSearch.setQueryCurrent(jsonObject.get("query_current").getAsLong());
        indexSearch.setFetchTotal(jsonObject.get("fetch_total").getAsLong());
        indexSearch.setFetchTimeInMillis(jsonObject.get("fetch_time_in_millis").getAsLong());
        indexSearch.setFetchCurrent(jsonObject.get("fetch_current").getAsLong());
        indexSearch.setScrollTotal(jsonObject.get("scroll_total").getAsLong());
        indexSearch.setScrollTimeInMillis(jsonObject.get("scroll_time_in_millis").getAsLong());
        indexSearch.setScrollCurrent(jsonObject.get("scroll_current").getAsLong());
        indexSearch.setSuggestTotal(jsonObject.get("suggest_total").getAsLong());
        indexSearch.setSuggestTimeInMillis(jsonObject.get("suggest_time_in_millis").getAsLong());
        indexSearch.setSuggestCurrent(jsonObject.get("suggest_current").getAsLong());
        return indexSearch;
    }

    public long getOpenContexts() {
        return openContexts;
    }

    public void setOpenContexts(long openContexts) {
        this.openContexts = openContexts;
    }

    public long getQueryTotal() {
        return queryTotal;
    }

    public void setQueryTotal(long queryTotal) {
        this.queryTotal = queryTotal;
    }

    public long getQueryTimeInMillis() {
        return queryTimeInMillis;
    }

    public void setQueryTimeInMillis(long queryTimeInMillis) {
        this.queryTimeInMillis = queryTimeInMillis;
    }

    public long getQueryCurrent() {
        return queryCurrent;
    }

    public void setQueryCurrent(long queryCurrent) {
        this.queryCurrent = queryCurrent;
    }

    public long getFetchTotal() {
        return fetchTotal;
    }

    public void setFetchTotal(long fetchTotal) {
        this.fetchTotal = fetchTotal;
    }

    public long getFetchTimeInMillis() {
        return fetchTimeInMillis;
    }

    public void setFetchTimeInMillis(long fetchTimeInMillis) {
        this.fetchTimeInMillis = fetchTimeInMillis;
    }

    public long getFetchCurrent() {
        return fetchCurrent;
    }

    public void setFetchCurrent(long fetchCurrent) {
        this.fetchCurrent = fetchCurrent;
    }

    public long getScrollTotal() {
        return scrollTotal;
    }

    public void setScrollTotal(long scrollTotal) {
        this.scrollTotal = scrollTotal;
    }

    public long getScrollTimeInMillis() {
        return scrollTimeInMillis;
    }

    public void setScrollTimeInMillis(long scrollTimeInMillis) {
        this.scrollTimeInMillis = scrollTimeInMillis;
    }

    public long getScrollCurrent() {
        return scrollCurrent;
    }

    public void setScrollCurrent(long scrollCurrent) {
        this.scrollCurrent = scrollCurrent;
    }

    public long getSuggestTotal() {
        return suggestTotal;
    }

    public void setSuggestTotal(long suggestTotal) {
        this.suggestTotal = suggestTotal;
    }

    public long getSuggestTimeInMillis() {
        return suggestTimeInMillis;
    }

    public void setSuggestTimeInMillis(long suggestTimeInMillis) {
        this.suggestTimeInMillis = suggestTimeInMillis;
    }

    public long getSuggestCurrent() {
        return suggestCurrent;
    }

    public void setSuggestCurrent(long suggestCurrent) {
        this.suggestCurrent = suggestCurrent;
    }
}
