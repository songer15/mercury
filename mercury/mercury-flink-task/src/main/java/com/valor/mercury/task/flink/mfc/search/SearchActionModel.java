package com.valor.mercury.task.flink.mfc.search;

/**
 * @author Gavin
 * 2019/10/10 9:45
 */
public class SearchActionModel {
    private String searchMsg;
    private String device;
    private String loginType;
    private String userId;
    private String countryCode;
    private String lan;
    private Long actionTime;
    private String appVersion;
    private Long vendorId;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getSearchMsg() {
        return searchMsg;
    }

    public void setSearchMsg(String searchMsg) {
        this.searchMsg = searchMsg;
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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }
}
