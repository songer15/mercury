package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexStore {
    private long sizeInBytes;

    public static IndexStore toIndexStore(JsonObject jsonObject) {
        IndexStore indexStore = new IndexStore();
        indexStore.setSizeInBytes(jsonObject.get("size_in_bytes").getAsLong());
        return indexStore;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
