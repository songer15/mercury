package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexCompletion {
    private long sizeInBytes;


    public static IndexCompletion toIndexCompletion(JsonObject jsonObject) {
        IndexCompletion indexCompletion = new IndexCompletion();
        indexCompletion.setSizeInBytes(jsonObject.get("size_in_bytes").getAsLong());
        return indexCompletion;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
