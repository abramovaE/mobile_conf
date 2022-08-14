package com.kotofeya.mobileconfigurator.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;

import java.util.List;

public class TakeInfoFull {

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("serial")
    @Expose
    private String serial;
    @SerializedName("followServer")
    @Expose
    private String followServer;
    @SerializedName("replyInterval")
    @Expose
    private String replyInterval;
    @SerializedName("hasPing")
    @Expose
    private Boolean hasPing;
    @SerializedName("systemTime")
    @Expose
    private String systemTime;
    @SerializedName("uptime")
    @Expose
    private String uptime;
    @SerializedName("load1min")
    @Expose
    private String load1min;
    @SerializedName("load5min")
    @Expose
    private String load5min;
    @SerializedName("load15min")
    @Expose
    private String load15min;
    @SerializedName("cpuFreq")
    @Expose
    private String cpuFreq;
    @SerializedName("cpuTemperature")
    @Expose
    private String cpuTemperature;
    @SerializedName("freeRam")
    @Expose
    private Integer freeRam;
    @SerializedName("bleMac")
    @Expose
    private String bleMac;
    @SerializedName("blePidHci0")
    @Expose
    private Integer blePidHci0;

    @SerializedName("bleIntervalMin")
    @Expose
    @Since(1.7)
    private Integer bleIntervalMin;

    @SerializedName("bleIntervalMax")
    @Expose
    @Since(1.7)
    private Integer bleIntervalMax;

    @SerializedName("bleScanIntervalScan")
    @Expose
    @Since(1.7)
    private Integer bleScanIntervalScan;

    @SerializedName("bleScanIntervalWindow")
    @Expose
    @Since(1.7)
    private Integer bleScanIntervalWindow;

    @SerializedName("interfaces")
    @Expose
    private List<TakeInfoInterface> interfaces = null;
    @SerializedName("scUartVer")
    @Expose
    private String scUartVer;
    @SerializedName("scUartPid")
    @Expose
    private Integer scUartPid;
    @SerializedName("boardVersion")
    @Expose
    private String boardVersion;
    @SerializedName("stmFirmware")
    @Expose
    private String stmFirmware;
    @SerializedName("stmBootload")
    @Expose
    private String stmBootload;
    @SerializedName("coreLinux")
    @Expose
    private String coreLinux;
    @SerializedName("compileDate")
    @Expose
    private String compileDate;
    @SerializedName("incrCity")
    @Expose
    private String incrCity;
    @SerializedName("stopTransLocate")
    @Expose
    private String stopTransLocate;
    @SerializedName("transpContents")
    @Expose
    private List<TakeInfoTranspContent> transpContents = null;
    @SerializedName("statContents")
    @Expose
    private List<TakeInfoStatContent> statContents = null;
    @SerializedName("logPath")
    @Expose
    private String logPath;
    @SerializedName("dailyRebootTime")
    @Expose
    private String dailyRebootTime;
    @SerializedName("critCpuLoad")
    @Expose
    private String critCpuLoad;
    @SerializedName("critRamFree")
    @Expose
    private String critRamFree;
    @SerializedName("crontabTasks")
    @Expose
    private List<String> crontabTasks = null;
    @SerializedName("lastReboot")
    @Expose
    private String lastReboot;

    @SerializedName("content")
    @Expose
    private String content;


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getFollowServer() {
        return followServer;
    }

    public void setFollowServer(String followServer) {
        this.followServer = followServer;
    }

    public String getReplyInterval() {
        return replyInterval;
    }

    public void setReplyInterval(String replyInterval) {
        this.replyInterval = replyInterval;
    }

    public Boolean getHasPing() {
        return hasPing;
    }

    public void setHasPing(Boolean hasPing) {
        this.hasPing = hasPing;
    }

    public String getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(String systemTime) {
        this.systemTime = systemTime;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getLoad1min() {
        return load1min;
    }

    public void setLoad1min(String load1min) {
        this.load1min = load1min;
    }

    public String getLoad5min() {
        return load5min;
    }

    public void setLoad5min(String load5min) {
        this.load5min = load5min;
    }

    public String getLoad15min() {
        return load15min;
    }

    public void setLoad15min(String load15min) {
        this.load15min = load15min;
    }

    public String getCpuFreq() {
        return cpuFreq;
    }

    public void setCpuFreq(String cpuFreq) {
        this.cpuFreq = cpuFreq;
    }

    public String getCpuTemperature() {
        return cpuTemperature;
    }

    public void setCpuTemperature(String cpuTemperature) {
        this.cpuTemperature = cpuTemperature;
    }

    public Integer getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(Integer freeRam) {
        this.freeRam = freeRam;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
    }

    public Integer getBlePidHci0() {
        return blePidHci0;
    }

    public void setBlePidHci0(Integer blePidHci0) {
        this.blePidHci0 = blePidHci0;
    }

    public List<TakeInfoInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<TakeInfoInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public String getScUartVer() {
        return scUartVer;
    }

    public void setScUartVer(String scUartVer) {
        this.scUartVer = scUartVer;
    }

    public Integer getScUartPid() {
        return scUartPid;
    }

    public void setScUartPid(Integer scUartPid) {
        this.scUartPid = scUartPid;
    }

    public String getBoardVersion() {
        return boardVersion;
    }

    public void setBoardVersion(String boardVersion) {
        this.boardVersion = boardVersion;
    }

    public String getStmFirmware() {
        return stmFirmware;
    }

    public void setStmFirmware(String stmFirmware) {
        this.stmFirmware = stmFirmware;
    }

    public String getStmBootload() {
        return stmBootload;
    }

    public void setStmBootload(String stmBootload) {
        this.stmBootload = stmBootload;
    }

    public String getCoreLinux() {
        return coreLinux;
    }

    public void setCoreLinux(String coreLinux) {
        this.coreLinux = coreLinux;
    }

    public String getCompileDate() {
        return compileDate;
    }

    public void setCompileDate(String compileDate) {
        this.compileDate = compileDate;
    }

    public String getIncrCity() {
        return incrCity;
    }

    public void setIncrCity(String incrCity) {
        this.incrCity = incrCity;
    }

    public String getStopTransLocate() {
        return stopTransLocate;
    }

    public void setStopTransLocate(String stopTransLocate) {
        this.stopTransLocate = stopTransLocate;
    }

    public List<TakeInfoTranspContent> getTranspContents() {
        return transpContents;
    }

    public void setTranspContents(List<TakeInfoTranspContent> transpContents) {
        this.transpContents = transpContents;
    }

    public List<TakeInfoStatContent> getStatContents() {
        return statContents;
    }

    public void setStatContents(List<TakeInfoStatContent> statContents) {
        this.statContents = statContents;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getDailyRebootTime() {
        return dailyRebootTime;
    }

    public void setDailyRebootTime(String dailyRebootTime) {
        this.dailyRebootTime = dailyRebootTime;
    }

    public String getCritCpuLoad() {
        return critCpuLoad;
    }

    public void setCritCpuLoad(String critCpuLoad) {
        this.critCpuLoad = critCpuLoad;
    }

    public String getCritRamFree() {
        return critRamFree;
    }

    public void setCritRamFree(String critRamFree) {
        this.critRamFree = critRamFree;
    }

    public List<String> getCrontabTasks() {
        return crontabTasks;
    }

    public void setCrontabTasks(List<String> crontabTasks) {
        this.crontabTasks = crontabTasks;
    }

    public String getLastReboot() {
        return lastReboot;
    }

    public void setLastReboot(String lastReboot) {
        this.lastReboot = lastReboot;
    }


    @Override
    public String toString() {
        return "type: " + type + '\n' +
                "serial: " + serial + '\n'
                +
                "followServer: " + followServer + '\n' +
                "replyInterval: " + replyInterval + '\n' +
                "hasPing: " + hasPing + '\n' +
                "systemTime: " + systemTime + '\n' +
                "uptime: " + uptime + '\n' +
                "load1min: " + load1min + '\n' +
                "load5min: " + load5min + '\n' +
                "load15min: " + load15min + '\n' +
                "cpuFreq: " + cpuFreq + '\n' +
                "cpuTemperature: " + cpuTemperature + '\n' +
                "freeRam: " + freeRam + '\n' +
                "bleMac: " + bleMac + '\n' +
                "blePidHci0: " + blePidHci0 + '\n' +
                "bleIntervalMin: " + bleIntervalMin + '\n' +
                "bleIntervalMax: " + bleIntervalMax + '\n' +
                "bleScanIntervalScan: " + bleScanIntervalScan + '\n' +
                "bleScanIntervalWindow: " + bleScanIntervalWindow + '\n' +
                "interfaces: " + interfaces + '\n' +
                "scUartVer: " + scUartVer + '\n' +
                "scUartPid: " + scUartPid + '\n' +
                "boardVersion: " + boardVersion + '\n' +
                "stmFirmware: " + stmFirmware + '\n' +
                "stmBootload: " + stmBootload + '\n' +
                "coreLinux: " + coreLinux + '\n' +
                "compileDate: " + compileDate + '\n' +
                "incrCity: " + incrCity + '\n' +
                "stopTransLocate: " + stopTransLocate + '\n' +
                "transpContents: " + transpContents + '\n' +
                "statContents: " + statContents + '\n' +
                "logPath: " + logPath + '\n' +
                "dailyRebootTime: " + dailyRebootTime + '\n' +
                "critCpuLoad: " + critCpuLoad + '\n' +
                "critRamFree: " + critRamFree + '\n' +
                "crontabTasks: " + crontabTasks + '\n' +
                "lastReboot: " + lastReboot + '\n';
    }

    public TakeInfoFull() {}
}
