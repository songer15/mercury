package com.valor.mercury.task.flink.devops.pm;

/**
 * @author Gavin
 * 2021/1/21 18:45
 */
public class ConnectedStreamData {
    private String did;
    private String selfDid;
    private String type;
    private String playType;
    private String playTtNum;
    private String flag;
    private String isPrepare;
    private String appVer;


    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getIsPrepare() {
        return isPrepare;
    }

    public void setIsPrepare(String isPrepare) {
        this.isPrepare = isPrepare;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPlayTtNum() {
        return playTtNum;
    }

    public void setPlayTtNum(String playTtNum) {
        this.playTtNum = playTtNum;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getSelfDid() {
        return selfDid;
    }

    public void setSelfDid(String selfDid) {
        this.selfDid = selfDid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
