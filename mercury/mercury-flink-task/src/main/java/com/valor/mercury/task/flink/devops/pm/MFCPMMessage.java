package com.valor.mercury.task.flink.devops.pm;

import org.elasticsearch.common.Strings;

/**
 * @author Gavin
 * 2020/4/9 14:27
 */
public class MFCPMMessage {
    private String selfDid;
    private String playHash;
    private String playNetDisk;
    private String playTtNum;
    private Double recvBkFromPmT;
    private Double recvBkFromPeerT;
    private Double recvBkFromStorageT;
    private Double recvBkFromPmP;
    private Double recvBkFromPeerP;
    private Double recvBkFromStorageP;
    private Double peerNums;
    private String cacheType;
    private Integer playBufferingTimes;
    private Integer useTag;
    private Double shareRate;
    private String engineVersion;
    private String appVer;
    private String appName;
    private String brand;
    private String model;
    private String mac;
    private String android;
    private String appId;
    private String playType;

    private String flag;

    //该资源是缓存还是在播
    private String resourceType;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getAndroid() {
        return android;
    }

    public void setAndroid(String android) {
        this.android = android;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public Integer getUseTag() {
        return useTag;
    }

    public void setUseTag(Integer useTag) {
        this.useTag = useTag;
    }

    public Double getShareRate() {
        return shareRate;
    }

    public void setShareRate(Double shareRate) {
        this.shareRate = shareRate;
    }

    public Integer getPlayBufferingTimes() {
        return playBufferingTimes;
    }

    public void setPlayBufferingTimes(Integer playBufferingTimes) {
        this.playBufferingTimes = playBufferingTimes;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public Double getPeerNums() {
        return peerNums;
    }

    public void setPeerNums(Double peerNums) {
        this.peerNums = peerNums;
    }

    @Override
    public String toString() {
        return "MFCPMMessage{" +
                "selfDid='" + selfDid + '\'' +
                ", playHash='" + playHash + '\'' +
                ", playNetDisk='" + playNetDisk + '\'' +
                ", playTtNum='" + playTtNum + '\'' +
                ", recvBkFromPmT=" + recvBkFromPmT +
                ", recvBkFromPeerT=" + recvBkFromPeerT +
                ", recvBkFromStorageT=" + recvBkFromStorageT +
                ", recvBkFromPmP=" + recvBkFromPmP +
                ", recvBkFromPeerP=" + recvBkFromPeerP +
                ", recvBkFromStorageP=" + recvBkFromStorageP +
                '}';
    }

    public String getSelfDid() {
        return selfDid;
    }

    public void setSelfDid(String selfDid) {
        this.selfDid = selfDid;
    }

    public String getPlayHash() {
        return playHash;
    }

    public void setPlayHash(String playHash) {
        this.playHash = playHash;
    }

    public String getPlayNetDisk() {
        return playNetDisk;
    }

    public void setPlayNetDisk(String playNetDisk) {
        this.playNetDisk = playNetDisk;
    }

    public String getPlayTtNum() {
        return playTtNum;
    }

    public void setPlayTtNum(String playTtNum) {
        this.playTtNum = playTtNum;
    }

    public Double getRecvBkFromPmT() {
        return recvBkFromPmT;
    }

    public void setRecvBkFromPmT(Double recvBkFromPmT) {
        this.recvBkFromPmT = recvBkFromPmT;
    }

    public Double getRecvBkFromPeerT() {
        return recvBkFromPeerT;
    }

    public void setRecvBkFromPeerT(Double recvBkFromPeerT) {
        this.recvBkFromPeerT = recvBkFromPeerT;
    }

    public Double getRecvBkFromStorageT() {
        return recvBkFromStorageT;
    }

    public void setRecvBkFromStorageT(Double recvBkFromStorageT) {
        this.recvBkFromStorageT = recvBkFromStorageT;
    }

    public Double getRecvBkFromPmP() {
        return recvBkFromPmP;
    }

    public void setRecvBkFromPmP(Double recvBkFromPmP) {
        this.recvBkFromPmP = recvBkFromPmP;
    }

    public Double getRecvBkFromPeerP() {
        return recvBkFromPeerP;
    }

    public void setRecvBkFromPeerP(Double recvBkFromPeerP) {
        this.recvBkFromPeerP = recvBkFromPeerP;
    }

    public Double getRecvBkFromStorageP() {
        return recvBkFromStorageP;
    }

    public void setRecvBkFromStorageP(Double recvBkFromStorageP) {
        this.recvBkFromStorageP = recvBkFromStorageP;
    }

    public boolean verify() {
        boolean vry = true;
        if (Strings.isEmpty(playHash)) {
            playHash = "empty";
            vry = false;
        }
        if (Strings.isEmpty(playTtNum)) {
            playTtNum = "empty";
            vry = false;
        }
        if (Strings.isEmpty(playNetDisk) || playNetDisk.equals("null")) {
            playNetDisk = "empty";
            vry = false;
        }
        if (Strings.isEmpty(cacheType)) {
            cacheType = "empty";
        }
        return vry;
    }
}
