package com.valor.mercury.task.flink.mfc.login;

import java.io.Serializable;

/**
 * @author Gavin
 * 2019/10/17 16:11
 */
public class UpgradeActionModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String loginType;
    private String userId;
    private String device;
    private Long actionTime;
    private Long vendorId;
    private Long appVersion;
    private Long preAppVersion;
    private String did;

    public UpgradeActionModel(String loginType, String userId, String device, Long actionTime, Long vendorId, Long appVersion, Long preAppVersion, String did) {
        this.loginType = loginType;
        this.userId = userId;
        this.device = device;
        this.actionTime = actionTime;
        this.vendorId = vendorId;
        this.appVersion = appVersion;
        this.preAppVersion = preAppVersion;
        this.did = did;
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

    public Long getPreAppVersion() {
        return preAppVersion;
    }

    public void setPreAppVersion(Long preAppVersion) {
        this.preAppVersion = preAppVersion;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }
}
