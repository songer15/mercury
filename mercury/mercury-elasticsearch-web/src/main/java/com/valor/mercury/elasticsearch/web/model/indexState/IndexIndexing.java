package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexIndexing {
    private long indexTotal;
    private long indexTimeInMillis;
    private long indexCurrent;
    private long indexFailed;
    private long deleteTotal;
    private long deleteTimeInMillis;
    private long deleteCurrent;
    private long noopUpdateTotal;
    private boolean throttled;
    private long throttledTimeInMillis;

    public static IndexIndexing toIndexIndexing(JsonObject jsonObject) {
        IndexIndexing indexIndexing = new IndexIndexing();
        indexIndexing.setIndexTotal(jsonObject.get("index_total").getAsLong());
        indexIndexing.setIndexTimeInMillis(jsonObject.get("index_time_in_millis").getAsLong());
        indexIndexing.setIndexCurrent(jsonObject.get("index_current").getAsLong());
        indexIndexing.setIndexFailed(jsonObject.get("index_failed").getAsLong());
        indexIndexing.setDeleteTotal(jsonObject.get("delete_total").getAsLong());
        indexIndexing.setDeleteTimeInMillis(jsonObject.get("delete_time_in_millis").getAsLong());
        indexIndexing.setDeleteCurrent(jsonObject.get("delete_current").getAsLong());
        indexIndexing.setNoopUpdateTotal(jsonObject.get("noop_update_total").getAsLong());
        indexIndexing.setThrottled(jsonObject.get("is_throttled").getAsBoolean());
        indexIndexing.setThrottledTimeInMillis(jsonObject.get("throttle_time_in_millis").getAsLong());
        return indexIndexing;
    }

    public long getIndexTotal() {
        return indexTotal;
    }

    public void setIndexTotal(long indexTotal) {
        this.indexTotal = indexTotal;
    }

    public long getIndexTimeInMillis() {
        return indexTimeInMillis;
    }

    public void setIndexTimeInMillis(long indexTimeInMillis) {
        this.indexTimeInMillis = indexTimeInMillis;
    }

    public long getIndexCurrent() {
        return indexCurrent;
    }

    public void setIndexCurrent(long indexCurrent) {
        this.indexCurrent = indexCurrent;
    }

    public long getIndexFailed() {
        return indexFailed;
    }

    public void setIndexFailed(long indexFailed) {
        this.indexFailed = indexFailed;
    }

    public long getDeleteTotal() {
        return deleteTotal;
    }

    public void setDeleteTotal(long deleteTotal) {
        this.deleteTotal = deleteTotal;
    }

    public long getDeleteTimeInMillis() {
        return deleteTimeInMillis;
    }

    public void setDeleteTimeInMillis(long deleteTimeInMillis) {
        this.deleteTimeInMillis = deleteTimeInMillis;
    }

    public long getDeleteCurrent() {
        return deleteCurrent;
    }

    public void setDeleteCurrent(long deleteCurrent) {
        this.deleteCurrent = deleteCurrent;
    }

    public long getNoopUpdateTotal() {
        return noopUpdateTotal;
    }

    public void setNoopUpdateTotal(long noopUpdateTotal) {
        this.noopUpdateTotal = noopUpdateTotal;
    }

    public boolean isThrottled() {
        return throttled;
    }

    public void setThrottled(boolean throttled) {
        this.throttled = throttled;
    }

    public long getThrottledTimeInMillis() {
        return throttledTimeInMillis;
    }

    public void setThrottledTimeInMillis(long throttledTimeInMillis) {
        this.throttledTimeInMillis = throttledTimeInMillis;
    }
}
