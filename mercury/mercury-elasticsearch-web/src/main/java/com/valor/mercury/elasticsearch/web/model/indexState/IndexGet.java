package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexGet {
    private long total;
    private long timeInMillis;
    private long existsTotal;
    private long existsTimeInMillis;
    private long missingTotal;
    private long missTimeInMillis;
    private long current;

    public static IndexGet toIndexGet(JsonObject jsonObject) {
        IndexGet indexGet = new IndexGet();
        indexGet.setTotal(jsonObject.get("total").getAsLong());
        indexGet.setTimeInMillis(jsonObject.get("time_in_millis").getAsLong());
        indexGet.setExistsTotal(jsonObject.get("exists_total").getAsLong());
        indexGet.setExistsTimeInMillis(jsonObject.get("exists_time_in_millis").getAsLong());
        indexGet.setMissingTotal(jsonObject.get("missing_total").getAsLong());
        indexGet.setMissTimeInMillis(jsonObject.get("missing_time_in_millis").getAsLong());
        indexGet.setCurrent(jsonObject.get("current").getAsLong());
        return indexGet;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public long getExistsTotal() {
        return existsTotal;
    }

    public void setExistsTotal(long existsTotal) {
        this.existsTotal = existsTotal;
    }

    public long getExistsTimeInMillis() {
        return existsTimeInMillis;
    }

    public void setExistsTimeInMillis(long existsTimeInMillis) {
        this.existsTimeInMillis = existsTimeInMillis;
    }

    public long getMissingTotal() {
        return missingTotal;
    }

    public void setMissingTotal(long missingTotal) {
        this.missingTotal = missingTotal;
    }

    public long getMissTimeInMillis() {
        return missTimeInMillis;
    }

    public void setMissTimeInMillis(long missTimeInMillis) {
        this.missTimeInMillis = missTimeInMillis;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }
}
