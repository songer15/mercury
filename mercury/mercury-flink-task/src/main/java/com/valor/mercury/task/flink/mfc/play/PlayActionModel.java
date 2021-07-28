package com.valor.mercury.task.flink.mfc.play;

/**
 * @author Gavin
 * 2019/9/23 9:30
 */
public class PlayActionModel {
    private String videoType;
    private Long playDuration;
    private String videoId;
    private String se;
    private String loginType;
    private String device;
    private Long actionTime;
    private Long vendorId;
    private String appVersion;
    private String lan;
    private String actionDetail;
    private String actionType;
    private String countryCode;
    private String userId;
    private int playTag;
    private String macSub;

    public String getMacSub() {
        return macSub;
    }

    public void setMacSub(String macSub) {
        this.macSub = macSub;
    }

    public int getPlayTag() {
        return playTag;
    }

    public void setPlayTag(int playTag) {
        this.playTag = playTag;
    }

    public String getSe() {
        return se;
    }

    public void setSe(String se) {
        this.se = se;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public Long getPlayDuration() {
        return playDuration;
    }

    public void setPlayDuration(Long playDuration) {
        this.playDuration = playDuration;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

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

    public Long getActionTime() {
        return actionTime;
    }

    public void setActionTime(Long actionTime) {
        this.actionTime = actionTime;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }

    public String getActionDetail() {
        return actionDetail;
    }

    public void setActionDetail(String actionDetail) {
        this.actionDetail = actionDetail;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
