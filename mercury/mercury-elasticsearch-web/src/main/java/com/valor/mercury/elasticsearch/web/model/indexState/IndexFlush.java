package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexFlush {
    private long total;
    private long periodic;
    private long totalTimeInMillis;

    public static IndexFlush toIndexFlush(JsonObject jsonObject) {
        IndexFlush indexFlush = new IndexFlush();
        indexFlush.setTotal(jsonObject.get("total").getAsLong());
        indexFlush.setPeriodic(jsonObject.get("periodic").getAsLong());
        indexFlush.setTotalTimeInMillis(jsonObject.get("total_time_in_millis").getAsLong());
        return indexFlush;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPeriodic() {
        return periodic;
    }

    public void setPeriodic(long periodic) {
        this.periodic = periodic;
    }

    public long getTotalTimeInMillis() {
        return totalTimeInMillis;
    }

    public void setTotalTimeInMillis(long totalTimeInMillis) {
        this.totalTimeInMillis = totalTimeInMillis;
    }
}
