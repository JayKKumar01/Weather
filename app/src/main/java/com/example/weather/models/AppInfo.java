package com.example.weather.models;

public class AppInfo {
    String appId;
    String appCertificate;

    public AppInfo() {
    }

    public AppInfo(String appId, String appCertificate) {
        this.appId = appId;
        this.appCertificate = appCertificate;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppCertificate() {
        return appCertificate;
    }

    public void setAppCertificate(String appCertificate) {
        this.appCertificate = appCertificate;
    }
}
