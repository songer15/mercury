package com.valor.mercury.elasticsearch.web.model.indexState;


import com.google.gson.JsonObject;

public class IndexState {
    private IndexDocs indexDocs;
    private IndexStore indexStore;
    private IndexIndexing indexIndexing;
    private IndexGet indexGet;
    private IndexSearch indexSearch;
    private IndexMerges indexMerges;
    private IndexRefresh indexRefresh;
    private IndexFlush indexFlush;
    private IndexWarmer indexWarmer;
    private IndexQueryCache indexQueryCache;
    private IndexFieldData indexFieldData;
    private IndexCompletion indexCompletion;
    private IndexSegments indexSegments;
    private IndexTranslog indexTranslog;
    private IndexRequestCache indexRequestCache;
    private IndexRecovery indexRecovery;

    public static IndexState toIndexState(JsonObject jsonObject) {
        IndexState indexState = new IndexState();
        indexState.setIndexDocs(IndexDocs.toIndexDocs(jsonObject.getAsJsonObject("docs")));
        indexState.setIndexStore(IndexStore.toIndexStore(jsonObject.getAsJsonObject("store")));
        indexState.setIndexIndexing(IndexIndexing.toIndexIndexing(jsonObject.getAsJsonObject("indexing")));
        indexState.setIndexGet(IndexGet.toIndexGet(jsonObject.getAsJsonObject("get")));
        indexState.setIndexSearch(IndexSearch.toIndexSearch(jsonObject.getAsJsonObject("search")));
        indexState.setIndexMerges(IndexMerges.toIndexMerges(jsonObject.getAsJsonObject("merges")));
        indexState.setIndexRefresh(IndexRefresh.toIndexRefresh(jsonObject.getAsJsonObject("refresh")));
        indexState.setIndexFlush(IndexFlush.toIndexFlush(jsonObject.getAsJsonObject("flush")));
        indexState.setIndexWarmer(IndexWarmer.toIndexWarmer(jsonObject.getAsJsonObject("warmer")));
        indexState.setIndexQueryCache(IndexQueryCache.toIndexQueryCache(jsonObject.getAsJsonObject("query_cache")));
        indexState.setIndexFieldData(IndexFieldData.toIndexFieldData(jsonObject.getAsJsonObject("fielddata")));
        indexState.setIndexCompletion(IndexCompletion.toIndexCompletion(jsonObject.getAsJsonObject("completion")));
        indexState.setIndexSegments(IndexSegments.toIndexSegments(jsonObject.getAsJsonObject("segments")));
        indexState.setIndexTranslog(IndexTranslog.toIndexTranslog(jsonObject.getAsJsonObject("translog")));
        indexState.setIndexRequestCache(IndexRequestCache.toIndexRequestCache(jsonObject.getAsJsonObject("request_cache")));
        indexState.setIndexRecovery(IndexRecovery.toIndexRecovery(jsonObject.getAsJsonObject("recovery")));

        return indexState;

    }


    public IndexDocs getIndexDocs() {
        return indexDocs;
    }

    public void setIndexDocs(IndexDocs indexDocs) {
        this.indexDocs = indexDocs;
    }

    public IndexStore getIndexStore() {
        return indexStore;
    }

    public void setIndexStore(IndexStore indexStore) {
        this.indexStore = indexStore;
    }

    public IndexIndexing getIndexIndexing() {
        return indexIndexing;
    }

    public void setIndexIndexing(IndexIndexing indexIndexing) {
        this.indexIndexing = indexIndexing;
    }

    public IndexGet getIndexGet() {
        return indexGet;
    }

    public void setIndexGet(IndexGet indexGet) {
        this.indexGet = indexGet;
    }

    public IndexSearch getIndexSearch() {
        return indexSearch;
    }

    public void setIndexSearch(IndexSearch indexSearch) {
        this.indexSearch = indexSearch;
    }

    public IndexMerges getIndexMerges() {
        return indexMerges;
    }

    public void setIndexMerges(IndexMerges indexMerges) {
        this.indexMerges = indexMerges;
    }

    public IndexRefresh getIndexRefresh() {
        return indexRefresh;
    }

    public void setIndexRefresh(IndexRefresh indexRefresh) {
        this.indexRefresh = indexRefresh;
    }

    public IndexFlush getIndexFlush() {
        return indexFlush;
    }

    public void setIndexFlush(IndexFlush indexFlush) {
        this.indexFlush = indexFlush;
    }

    public IndexWarmer getIndexWarmer() {
        return indexWarmer;
    }

    public void setIndexWarmer(IndexWarmer indexWarmer) {
        this.indexWarmer = indexWarmer;
    }

    public IndexQueryCache getIndexQueryCache() {
        return indexQueryCache;
    }

    public void setIndexQueryCache(IndexQueryCache indexQueryCache) {
        this.indexQueryCache = indexQueryCache;
    }

    public IndexFieldData getIndexFieldData() {
        return indexFieldData;
    }

    public void setIndexFieldData(IndexFieldData indexFieldData) {
        this.indexFieldData = indexFieldData;
    }

    public IndexCompletion getIndexCompletion() {
        return indexCompletion;
    }

    public void setIndexCompletion(IndexCompletion indexCompletion) {
        this.indexCompletion = indexCompletion;
    }

    public IndexSegments getIndexSegments() {
        return indexSegments;
    }

    public void setIndexSegments(IndexSegments indexSegments) {
        this.indexSegments = indexSegments;
    }

    public IndexTranslog getIndexTranslog() {
        return indexTranslog;
    }

    public void setIndexTranslog(IndexTranslog indexTranslog) {
        this.indexTranslog = indexTranslog;
    }

    public IndexRequestCache getIndexRequestCache() {
        return indexRequestCache;
    }

    public void setIndexRequestCache(IndexRequestCache indexRequestCache) {
        this.indexRequestCache = indexRequestCache;
    }

    public IndexRecovery getIndexRecovery() {
        return indexRecovery;
    }

    public void setIndexRecovery(IndexRecovery indexRecovery) {
        this.indexRecovery = indexRecovery;
    }
}
