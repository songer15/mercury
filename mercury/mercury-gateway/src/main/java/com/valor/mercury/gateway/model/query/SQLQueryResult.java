package com.valor.mercury.gateway.model.query;


import com.valor.mercury.gateway.model.Row;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class SQLQueryResult extends QueryResult {

    /**
     * 返回结果大小
     */
    protected long resultSize;
    /**
     * 数据
     */
    protected List<Row> result;
    /**
     * 结果列
     */
    protected Set<String> resultColumns;

    /**
     * SQL 方法 (增删改查)
     */
    protected String sqlQueryMethod;

    public SQLQueryResult(){
        super();
    }

    protected SQLQueryResult(Builder builder) {
        super(builder);
        this.resultSize = builder.resultSize;
        this.result = builder.result;
        this.resultColumns = builder.resultColumns;
    }

    public long getResultSize() {
        return resultSize;
    }

    public void setResultSize(long resultSize) {
        this.resultSize = resultSize;
    }

    public List<Row> getResult() {
        return result;
    }

    public void setResult(List<Row> result) {
        this.result = result;
    }

    public Set<String> getResultColumns() {
        return resultColumns;
    }

    public void setResultColumns(Set<String> resultColumns) {
        this.resultColumns = resultColumns;
    }

    public String getSqlQueryMethod() {
        return sqlQueryMethod;
    }

    public void setSqlQueryMethod(String sqlQueryMethod) {
        this.sqlQueryMethod = sqlQueryMethod;
    }

    public static class Builder extends QueryResult.Builder<SQLQueryResult, Builder>{
        private long resultSize;
        private List<Row> result;
        private Set<String> resultColumns;

        public Builder resultSize(long resultSize) {
            this.resultSize = resultSize;
            return this;
        }

        public Builder result(List<Row> result) {
            this.result = result;
            return this;
        }

        public Builder resultColumns(Set<String> resultColumns) {
            this.resultColumns = resultColumns;
            return this;
        }

        public SQLQueryResult build() {
            return new SQLQueryResult(this);
        }

    }

}
