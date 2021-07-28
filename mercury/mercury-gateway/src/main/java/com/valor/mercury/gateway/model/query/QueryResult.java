package com.valor.mercury.gateway.model.query;

/**
 */
public class QueryResult {

    /**
     * 查询耗时
     */
    protected long time;

    /**
     * 查询结果总数
     */
    protected long total;

    /**
     * 输出文件路径
     */
    protected String filePath;

    public QueryResult(){

    }

    protected QueryResult(Builder builder) {
        this.time = builder.time;
        this.total = builder.total;
        this.filePath = builder.filePath;

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @SuppressWarnings("unchecked")
    abstract protected static class Builder<T extends QueryResult, K> {
        private long time;
        private long total;
        private String filePath;


        public K time(long time) {
            this.time = time;
            return (K)this;
        }

        public K total(long total) {
            this.total = total;
            return (K)this;
        }

        public K export(String export) {
            this.filePath = export;
            return (K)this;
        }

        abstract public T build();

    }

}
