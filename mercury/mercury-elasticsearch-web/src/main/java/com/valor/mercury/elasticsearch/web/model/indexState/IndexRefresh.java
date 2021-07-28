package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexRefresh {
    private long total;
    private long totalTimeInMillis;
    private long listeners;

    public static IndexRefresh toIndexRefresh(JsonObject jsonObject) {
        IndexRefresh indexRefresh = new IndexRefresh();
        indexRefresh.setTotal(jsonObject.get("total").getAsLong());
        indexRefresh.setTotalTimeInMillis(jsonObject.get("total_time_in_millis").getAsLong());
        indexRefresh.setListeners(jsonObject.get("listeners").getAsLong());
        return indexRefresh;
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

    public long getListeners() {
        return listeners;
    }

    public void setListeners(long listeners) {
        this.listeners = listeners;
    }
}
