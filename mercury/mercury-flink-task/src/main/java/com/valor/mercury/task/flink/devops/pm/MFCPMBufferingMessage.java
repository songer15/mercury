package com.valor.mercury.task.flink.devops.pm;


/**
 * @author Gavin
 * 2020/5/14 15:18
 */
public class MFCPMBufferingMessage {
    private String ttID;
    private String resourceSite;
    private String did;
    private Long bufferingStartTime;
    private Integer bufferingTime;
    private String appVer;
    private String appName;
    private String brand;
    private String model;
    private String mac;
    private String android;
    private String appId;

    private String isPrepare;
    private String playProtocol;
    private Long prepareFirstBufferingTime;

    private String flag;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Long getPrepareFirstBufferingTime() {
        return prepareFirstBufferingTime;
    }

    public void setPrepareFirstBufferingTime(Long prepareFirstBufferingTime) {
        this.prepareFirstBufferingTime = prepareFirstBufferingTime;
    }

    public String getIsPrepare() {
        return isPrepare;
    }

    public void setIsPrepare(String isPrepare) {
        this.isPrepare = isPrepare;
    }

    public String getPlayProtocol() {
        return playProtocol;
    }

    public void setPlayProtocol(String playProtocol) {
        this.playProtocol = playProtocol;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    public String getTtID() {
        return ttID;
    }

    public void setTtID(String ttID) {
        this.ttID = ttID;
    }

    public String getResourceSite() {
        return resourceSite;
    }

    public void setResourceSite(String resourceSite) {
        this.resourceSite = resourceSite;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public Long getBufferingStartTime() {
        return bufferingStartTime;
    }

    public void setBufferingStartTime(Long bufferingStartTime) {
        this.bufferingStartTime = bufferingStartTime;
    }

    public Integer getBufferingTime() {
        return bufferingTime;
    }

    public void setBufferingTime(Integer bufferingTime) {
        this.bufferingTime = bufferingTime;
    }
}
