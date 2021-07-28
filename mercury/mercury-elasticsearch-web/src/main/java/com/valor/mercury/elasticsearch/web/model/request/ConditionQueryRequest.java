package com.valor.mercury.elasticsearch.web.model.request;

public class ConditionQueryRequest {
    private String logicalOperator;
    private String groupname;
    private String subgroupname;
    private String rowlevel;
    private String conditionFieldVal;
    private String conditionOptionVal;
    private ConditionValue conditionValueVal = new ConditionValue();
    private ConditionValue conditionValueLeftVal = new ConditionValue();
    private ConditionValue conditionValueRightVal = new ConditionValue();

    public String getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getSubgroupname() {
        return subgroupname;
    }

    public void setSubgroupname(String subgroupname) {
        this.subgroupname = subgroupname;
    }

    public String getRowlevel() {
        return rowlevel;
    }

    public void setRowlevel(String rowlevel) {
        this.rowlevel = rowlevel;
    }

    public String getConditionFieldVal() {
        return conditionFieldVal;
    }

    public void setConditionFieldVal(String conditionFieldVal) {
        this.conditionFieldVal = conditionFieldVal;
    }

    public String getConditionOptionVal() {
        return conditionOptionVal;
    }

    public void setConditionOptionVal(String conditionOptionVal) {
        this.conditionOptionVal = conditionOptionVal;
    }

    public ConditionValue getConditionValueVal() {
        return conditionValueVal;
    }

    public void setConditionValueVal(ConditionValue conditionValueVal) {
        this.conditionValueVal = conditionValueVal;
    }

    public ConditionValue getConditionValueLeftVal() {
        return conditionValueLeftVal;
    }

    public void setConditionValueLeftVal(ConditionValue conditionValueLeftVal) {
        this.conditionValueLeftVal = conditionValueLeftVal;
    }

    public ConditionValue getConditionValueRightVal() {
        return conditionValueRightVal;
    }

    public void setConditionValueRightVal(ConditionValue conditionValueRightVal) {
        this.conditionValueRightVal = conditionValueRightVal;
    }
}
