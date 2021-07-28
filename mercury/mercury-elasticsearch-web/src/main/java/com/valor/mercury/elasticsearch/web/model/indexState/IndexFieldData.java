package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexFieldData {
    private long memorySizeInBytes;
    private long evictions;

    public static IndexFieldData toIndexFieldData(JsonObject jsonObject) {
        IndexFieldData indexFieldData = new IndexFieldData();
        indexFieldData.setMemorySizeInBytes(jsonObject.get("memory_size_in_bytes").getAsLong());
        indexFieldData.setEvictions(jsonObject.get("evictions").getAsLong());
        return indexFieldData;
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
}
