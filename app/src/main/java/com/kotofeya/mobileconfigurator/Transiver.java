package com.kotofeya.mobileconfigurator;

public class Transiver {

    private String ssid;
    private String version;
    private String stmFirmware;
    private String stmBootloader;
    private String basicScanInfo;

    private String uptime;
    private String ip;



    public Transiver(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStmFirmware() {
        return stmFirmware;
    }

    public void setStmFirmware(String stmFirmware) {
        this.stmFirmware = stmFirmware;
    }

    public String getStmBootloader() {
        return stmBootloader;
    }

    public void setStmBootloader(String stmBootloader) {
        this.stmBootloader = stmBootloader;
    }

    public String getBasicScanInfo() {
        return basicScanInfo;
    }

    public void setBasicScanInfo(String basicScanInfo) {
        this.basicScanInfo = basicScanInfo;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }
}
