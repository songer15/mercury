package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexRecovery {
    private long currentAsSource;
    private long currentAsTarget;
    private long throttleTimeInMillis;

    public static IndexRecovery toIndexRecovery(JsonObject jsonObject) {
        IndexRecovery indexRecovery = new IndexRecovery();
        indexRecovery.setCurrentAsSource(jsonObject.get("current_as_source").getAsLong());
        indexRecovery.setCurrentAsTarget(jsonObject.get("current_as_target").getAsLong());
        indexRecovery.setThrottleTimeInMillis(jsonObject.get("throttle_time_in_millis").getAsLong());
        return indexRecovery;
    }

    public long getCurrentAsSource() {
        return currentAsSource;
    }

    public void setCurrentAsSource(long currentAsSource) {
        this.currentAsSource = currentAsSource;
    }

    public long getCurrentAsTarget() {
        return currentAsTarget;
    }

    public void setCurrentAsTarget(long currentAsTarget) {
        this.currentAsTarget = currentAsTarget;
    }

    public long getThrottleTimeInMillis() {
        return throttleTimeInMillis;
    }

    public void setThrottleTimeInMillis(long throttleTimeInMillis) {
        this.throttleTimeInMillis = throttleTimeInMillis;
    }
}
