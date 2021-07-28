package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexMerges {
    private long current;
    private long currentDocs;
    private long currentSizeInBytes;
    private long total;
    private long totalTimeInMillis;
    private long totalDocs;
    private long totalSizeInBytes;
    private long totalStoppedTimeInMillis;
    private long totalThrottledTimeInMillis;
    private long totalAutoThrottleInBytes;

    public static IndexMerges toIndexMerges(JsonObject jsonObject) {
        IndexMerges indexMerges = new IndexMerges();
        indexMerges.setCurrent(jsonObject.get("current").getAsLong());
        indexMerges.setCurrentDocs(jsonObject.get("current_docs").getAsLong());
        indexMerges.setCurrentSizeInBytes(jsonObject.get("current_size_in_bytes").getAsLong());
        indexMerges.setTotal(jsonObject.get("total").getAsLong());
        indexMerges.setTotalTimeInMillis(jsonObject.get("total_time_in_millis").getAsLong());
        indexMerges.setTotalDocs(jsonObject.get("total_docs").getAsLong());
        indexMerges.setTotalSizeInBytes(jsonObject.get("total_size_in_bytes").getAsLong());
        indexMerges.setTotalStoppedTimeInMillis(jsonObject.get("total_stopped_time_in_millis").getAsLong());
        indexMerges.setTotalThrottledTimeInMillis(jsonObject.get("total_throttled_time_in_millis").getAsLong());
        indexMerges.setTotalAutoThrottleInBytes(jsonObject.get("total_auto_throttle_in_bytes").getAsLong());

        return indexMerges;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getCurrentDocs() {
        return currentDocs;
    }

    public void setCurrentDocs(long currentDocs) {
        this.currentDocs = currentDocs;
    }

    public long getCurrentSizeInBytes() {
        return currentSizeInBytes;
    }

    public void setCurrentSizeInBytes(long currentSizeInBytes) {
        this.currentSizeInBytes = currentSizeInBytes;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalTimeInMillis() {
        return totalTimeInMillis;
    }

    public void setTotalTimeInMillis(long totalTimeInMillis) {
        this.totalTimeInMillis = totalTimeInMillis;
    }

    public long getTotalDocs() {
        return totalDocs;
    }

    public void setTotalDocs(long totalDocs) {
        this.totalDocs = totalDocs;
    }

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(long totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public long getTotalStoppedTimeInMillis() {
        return totalStoppedTimeInMillis;
    }

    public void setTotalStoppedTimeInMillis(long totalStoppedTimeInMillis) {
        this.totalStoppedTimeInMillis = totalStoppedTimeInMillis;
    }

    public long getTotalThrottledTimeInMillis() {
        return totalThrottledTimeInMillis;
    }

    public void setTotalThrottledTimeInMillis(long totalThrottledTimeInMillis) {
        this.totalThrottledTimeInMillis = totalThrottledTimeInMillis;
    }

    public long getTotalAutoThrottleInBytes() {
        return totalAutoThrottleInBytes;
    }

    public void setTotalAutoThrottleInBytes(long totalAutoThrottleInBytes) {
        this.totalAutoThrottleInBytes = totalAutoThrottleInBytes;
    }
}
