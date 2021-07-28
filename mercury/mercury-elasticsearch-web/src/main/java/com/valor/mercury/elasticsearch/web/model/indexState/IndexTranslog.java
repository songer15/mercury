package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexTranslog {
    private long operations;
    private long sizeInBytes;
    private long uncommittedOperations;
    private long uncommittedSizeInBytes;
    private long earliestLastModifiedAge;

    public static IndexTranslog toIndexTranslog(JsonObject jsonObject) {
        IndexTranslog indexTranslog = new IndexTranslog();
        indexTranslog.setOperations(jsonObject.get("operations").getAsLong());
        indexTranslog.setSizeInBytes(jsonObject.get("size_in_bytes").getAsLong());
        indexTranslog.setUncommittedOperations(jsonObject.get("uncommitted_operations").getAsLong());
        indexTranslog.setUncommittedSizeInBytes(jsonObject.get("uncommitted_size_in_bytes").getAsLong());
        indexTranslog.setEarliestLastModifiedAge(jsonObject.get("earliest_last_modified_age").getAsLong());
        return indexTranslog;
    }

    public long getOperations() {
        return operations;
    }

    public void setOperations(long operations) {
        this.operations = operations;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public long getUncommittedOperations() {
        return uncommittedOperations;
    }

    public void setUncommittedOperations(long uncommittedOperations) {
        this.uncommittedOperations = uncommittedOperations;
    }

    public long getUncommittedSizeInBytes() {
        return uncommittedSizeInBytes;
    }

    public void setUncommittedSizeInBytes(long uncommittedSizeInBytes) {
        this.uncommittedSizeInBytes = uncommittedSizeInBytes;
    }

    public long getEarliestLastModifiedAge() {
        return earliestLastModifiedAge;
    }

    public void setEarliestLastModifiedAge(long earliestLastModifiedAge) {
        this.earliestLastModifiedAge = earliestLastModifiedAge;
    }
}
