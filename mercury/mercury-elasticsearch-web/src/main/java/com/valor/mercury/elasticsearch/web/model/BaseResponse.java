package com.valor.mercury.elasticsearch.web.model;

public class BaseResponse extends AbstractPrintable {
    private String checkSum;

    private long createTime;
    private long updateTime;
    private String lastModifyUser = "";

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getLastModifyUser() {
        return lastModifyUser;
    }

    public void setLastModifyUser(String lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }
    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }
}
