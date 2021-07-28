package com.valor.mercury.task.flink.btv.adv;

/**
 * @author Gavin.hu
 * 2019/4/18
 **/
public class AdvModel {
    private String loginType;
    private String device;
    private String email;
    private String uid;
    private String did;
    private String clientIp;
    private String appVersion;
    private Long actionTime;
    private String userStatus;
    private String releaseId;
    private Integer advPlacement;
    private Double browseCount;
    private Double clickCount;
    private Long duration;
    private String countryCode;

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Long getActionTime() {
        return actionTime;
    }

    public void setActionTime(Long actionTime) {
        this.actionTime = actionTime;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public Integer getAdvPlacement() {
        return advPlacement;
    }

    public void setAdvPlacement(Integer advPlacement) {
        this.advPlacement = advPlacement;
    }

    public Double getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(Double browseCount) {
        this.browseCount = browseCount;
    }

    public Double getClickCount() {
        return clickCount;
    }

    public void setClickCount(Double clickCount) {
        this.clickCount = clickCount;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
