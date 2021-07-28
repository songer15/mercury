package com.valor.mercury.task.flink.mfc.login;

import java.io.Serializable;

/**
 * @author Gavin
 * 2019/10/21 9:20
 */
public class LoginActionModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String loginType;
    private String userId;
    private String device;
    private Long actionTime;
    private Long vendorId;
    private Long appVersion;
    private String did;
    private String countryCode;
    private String macSub;

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

    public Long getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Long appVersion) {
        this.appVersion = appVersion;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getMacSub() {
        return macSub;
    }

    public void setMacSub(String macSub) {
        this.macSub = macSub;
    }
}
