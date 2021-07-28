package com.valor.mercury.elasticsearch.web.model.indexState;

import com.google.gson.JsonObject;

public class IndexSegments {
    private long count;
    private long memoryInBytes;
    private long termsMemoryInBytes;
    private long storedFieldsMemoryInBytes;
    private long termVectorsMemoryInBytes;
    private long normsMemoryInBytes;
    private long pointsMemoryInBytes;
    private long docValuesMemoryInBytes;
    private long indexWriterMemoryInBytes;
    private long versionMapMemoryInBytes;
    private long fixedBitSetMemoryInBytes;
    private long maxUnsafeAutoIdTimestamp;
    //private FileSizes fileSizes;

    public static IndexSegments toIndexSegments(JsonObject jsonObject) {
        IndexSegments indexSegments = new IndexSegments();
        indexSegments.setCount(jsonObject.get("count").getAsLong());
        indexSegments.setMemoryInBytes(jsonObject.get("memory_in_bytes").getAsLong());
        indexSegments.setTermsMemoryInBytes(jsonObject.get("terms_memory_in_bytes").getAsLong());
        indexSegments.setStoredFieldsMemoryInBytes(jsonObject.get("stored_fields_memory_in_bytes").getAsLong());
        indexSegments.setTermVectorsMemoryInBytes(jsonObject.get("term_vectors_memory_in_bytes").getAsLong());
        indexSegments.setNormsMemoryInBytes(jsonObject.get("norms_memory_in_bytes").getAsLong());
        indexSegments.setPointsMemoryInBytes(jsonObject.get("points_memory_in_bytes").getAsLong());
        indexSegments.setDocValuesMemoryInBytes(jsonObject.get("doc_values_memory_in_bytes").getAsLong());
        indexSegments.setIndexWriterMemoryInBytes(jsonObject.get("index_writer_memory_in_bytes").getAsLong());
        indexSegments.setVersionMapMemoryInBytes(jsonObject.get("version_map_memory_in_bytes").getAsLong());
        indexSegments.setFixedBitSetMemoryInBytes(jsonObject.get("fixed_bit_set_memory_in_bytes").getAsLong());
        indexSegments.setMaxUnsafeAutoIdTimestamp(jsonObject.get("max_unsafe_auto_id_timestamp").getAsLong());
        return indexSegments;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getMemoryInBytes() {
        return memoryInBytes;
    }

    public void setMemoryInBytes(long memoryInBytes) {
        this.memoryInBytes = memoryInBytes;
    }

    public long getTermsMemoryInBytes() {
        return termsMemoryInBytes;
    }

    public void setTermsMemoryInBytes(long termsMemoryInBytes) {
        this.termsMemoryInBytes = termsMemoryInBytes;
    }

    public long getStoredFieldsMemoryInBytes() {
        return storedFieldsMemoryInBytes;
    }

    public void setStoredFieldsMemoryInBytes(long storedFieldsMemoryInBytes) {
        this.storedFieldsMemoryInBytes = storedFieldsMemoryInBytes;
    }

    public long getTermVectorsMemoryInBytes() {
        return termVectorsMemoryInBytes;
    }

    public void setTermVectorsMemoryInBytes(long termVectorsMemoryInBytes) {
        this.termVectorsMemoryInBytes = termVectorsMemoryInBytes;
    }

    public long getNormsMemoryInBytes() {
        return normsMemoryInBytes;
    }

    public void setNormsMemoryInBytes(long normsMemoryInBytes) {
        this.normsMemoryInBytes = normsMemoryInBytes;
    }

    public long getPointsMemoryInBytes() {
        return pointsMemoryInBytes;
    }

    public void setPointsMemoryInBytes(long pointsMemoryInBytes) {
        this.pointsMemoryInBytes = pointsMemoryInBytes;
    }

    public long getDocValuesMemoryInBytes() {
        return docValuesMemoryInBytes;
    }

    public void setDocValuesMemoryInBytes(long docValuesMemoryInBytes) {
        this.docValuesMemoryInBytes = docValuesMemoryInBytes;
    }

    public long getIndexWriterMemoryInBytes() {
        return indexWriterMemoryInBytes;
    }

    public void setIndexWriterMemoryInBytes(long indexWriterMemoryInBytes) {
        this.indexWriterMemoryInBytes = indexWriterMemoryInBytes;
    }

    public long getVersionMapMemoryInBytes() {
        return versionMapMemoryInBytes;
    }

    public void setVersionMapMemoryInBytes(long versionMapMemoryInBytes) {
        this.versionMapMemoryInBytes = versionMapMemoryInBytes;
    }

    public long getFixedBitSetMemoryInBytes() {
        return fixedBitSetMemoryInBytes;
    }

    public void setFixedBitSetMemoryInBytes(long fixedBitSetMemoryInBytes) {
        this.fixedBitSetMemoryInBytes = fixedBitSetMemoryInBytes;
    }

    public long getMaxUnsafeAutoIdTimestamp() {
        return maxUnsafeAutoIdTimestamp;
    }

    public void setMaxUnsafeAutoIdTimestamp(long maxUnsafeAutoIdTimestamp) {
        this.maxUnsafeAutoIdTimestamp = maxUnsafeAutoIdTimestamp;
    }


}
