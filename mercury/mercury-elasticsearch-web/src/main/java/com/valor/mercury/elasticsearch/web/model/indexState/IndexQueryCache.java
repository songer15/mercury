package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexQueryCache {
    private long memorySizeInBytes;
    private long totalCount;
    private long hitCount;
    private long missCount;
    private long cacheSize;
    private long cacheCount;
    private long evictions;

    public static IndexQueryCache toIndexQueryCache(JsonObject jsonObject) {
        IndexQueryCache indexQueryCache = new IndexQueryCache();
        indexQueryCache.setMemorySizeInBytes(jsonObject.get("memory_size_in_bytes").getAsLong());
        indexQueryCache.setTotalCount(jsonObject.get("total_count").getAsLong());
        indexQueryCache.setHitCount(jsonObject.get("hit_count").getAsLong());
        indexQueryCache.setMissCount(jsonObject.get("miss_count").getAsLong());
        indexQueryCache.setCacheSize(jsonObject.get("cache_size").getAsLong());
        indexQueryCache.setCacheCount(jsonObject.get("cache_count").getAsLong());
        indexQueryCache.setEvictions(jsonObject.get("evictions").getAsLong());

        return indexQueryCache;
    }

    public long getMemorySizeInBytes() {
        return memorySizeInBytes;
    }

    public void setMemorySizeInBytes(long memorySizeInBytes) {
        this.memorySizeInBytes = memorySizeInBytes;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
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

    public long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public long getCacheCount() {
        return cacheCount;
    }

    public void setCacheCount(long cacheCount) {
        this.cacheCount = cacheCount;
    }

    public long getEvictions() {
        return evictions;
    }

    public void setEvictions(long evictions) {
        this.evictions = evictions;
    }
}
