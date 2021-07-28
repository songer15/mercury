package com.valor.mercury.task.flink.devops.pm;

import java.io.Serializable;

/**
 * @author Gavin
 * 2020/4/10 17:22
 */
public class MFCPMTopQueryResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private String key = "0";
    private String ttId;
    private Long count;
    private Long uniqueCount;
    private String playNetDisk;
    private Double shareRate;
    private String playHash;
    private Integer peerNums;
    private String cacheType;
    private Integer playBufferingTimes;
    private Double avg1;
    private Double avg2;
    private Double avg3;
    private Double avg4;
    private String appId;

    private String resourceType;

    private String playType;

    private Double recvBkFromPmT;
    private Double recvBkFromPeerT;
    private Double recvBkFromPmP;
    private Double recvBkFromPeerP;

    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Double getAvg4() {
        return avg4;
    }

    public void setAvg4(Double avg4) {
        this.avg4 = avg4;
    }

    public Double getAvg3() {
        return avg3;
    }

    public void setAvg3(Double avg3) {
        this.avg3 = avg3;
    }

    public Double getAvg1() {
        return avg1;
    }

    public void setAvg1(Double avg1) {
        this.avg1 = avg1;
    }

    public Double getAvg2() {
        return avg2;
    }

    public void setAvg2(Double avg2) {
        this.avg2 = avg2;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "MFCPMTopQueryResult{" +
                "ttId='" + ttId + '\'' +
                ", count=" + count +
                ", uniqueCount=" + uniqueCount +
                ", playNetDisk='" + playNetDisk + '\'' +
                ", shareRate=" + shareRate +
                ", playHash='" + playHash + '\'' +
                ", peerNums=" + peerNums +
                ", cacheType='" + cacheType + '\'' +
                ", playBufferingTimes=" + playBufferingTimes +
                '}';
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

    public Integer getPeerNums() {
        return peerNums;
    }

    public void setPeerNums(Integer peerNums) {
        this.peerNums = peerNums;
    }

    public void addCount(Long count) {
        this.count += count;
    }

    public void addUniqueCount(Long uniqueCount) {
        this.uniqueCount += uniqueCount;
    }

    public String getTtId() {
        return ttId;
    }

    public void setTtId(String ttId) {
        this.ttId = ttId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getUniqueCount() {
        return uniqueCount;
    }

    public void setUniqueCount(Long uniqueCount) {
        this.uniqueCount = uniqueCount;
    }

    public String getPlayNetDisk() {
        return playNetDisk;
    }

    public void setPlayNetDisk(String playNetDisk) {
        this.playNetDisk = playNetDisk;
    }

    public Double getShareRate() {
        return shareRate;
    }

    public void setShareRate(Double shareRate) {
        this.shareRate = shareRate;
    }

    public String getPlayHash() {
        return playHash;
    }

    public void setPlayHash(String playHash) {
        this.playHash = playHash;
    }
}
