package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexDocs {
    private long count;
    private long deleted;

    public static IndexDocs toIndexDocs(JsonObject jsonObject) {
        IndexDocs indexDocs = new IndexDocs();
        indexDocs.setCount(jsonObject.get("count").getAsLong());
        indexDocs.setDeleted(jsonObject.get("deleted").getAsLong());
        return indexDocs;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }


}
