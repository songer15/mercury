package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexRequestCache {
    private long memorySizeInBytes;
    private long evictions;
    private long hitCount;
    private long missCount;

    public static IndexRequestCache toIndexRequestCache(JsonObject jsonObject) {
        IndexRequestCache indexRequestCache = new IndexRequestCache();
        indexRequestCache.setMemorySizeInBytes(jsonObject.get("memory_size_in_bytes").getAsLong());
        indexRequestCache.setEvictions(jsonObject.get("evictions").getAsLong());
        indexRequestCache.setHitCount(jsonObject.get("hit_count").getAsLong());
        indexRequestCache.setMissCount(jsonObject.get("miss_count").getAsLong());

        return indexRequestCache;
    }

    public long getMemorySizeInBytes() {
        return memorySizeInBytes;
    }

    public void setMemorySizeInBytes(long memorySizeInBytes) {
        this.memorySizeInBytes = memorySizeInBytes;
    }

    public long getEvictions() {
        return evictions;
    }

    public void setEvictions(long evictions) {
        this.evictions = evictions;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }
}
