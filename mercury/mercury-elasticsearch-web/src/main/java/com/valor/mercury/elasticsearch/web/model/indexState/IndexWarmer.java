package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexWarmer {
    private long current;
    private long total;
    private long totalTimeInMillis;

    public static IndexWarmer toIndexWarmer(JsonObject jsonObject) {
        IndexWarmer indexWarmer = new IndexWarmer();
        indexWarmer.setCurrent(jsonObject.get("current").getAsLong());
        indexWarmer.setTotal(jsonObject.get("total").getAsLong());
        indexWarmer.setTotalTimeInMillis(jsonObject.get("total_time_in_millis").getAsLong());
        return indexWarmer;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
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
}
