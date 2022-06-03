package com.kotofeya.mobileconfigurator.network.post_response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;

import java.util.ArrayList;
import java.util.List;

public class TakeInfoFull implements Parcelable {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("serial")
    @Expose
    private Integer serial;
    @SerializedName("followServer")
    @Expose
    private String followServer;
    @SerializedName("replyInterval")
    @Expose
    private Integer replyInterval;
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
    private Integer cpuFreq;
    @SerializedName("cpuTemperature")
    @Expose
    private Integer cpuTemperature;
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
    private Integer incrCity;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSerial() {
        return serial;
    }
    public void setSerial(Integer serial) {
        this.serial = serial;
    }
    public String getFollowServer() {
        return followServer;
    }
    public void setFollowServer(String followServer) {
        this.followServer = followServer;
    }
    public Integer getReplyInterval() {
        return replyInterval;
    }
    public void setReplyInterval(Integer replyInterval) {
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
    public Integer getCpuFreq() {
        return cpuFreq;
    }
    public void setCpuFreq(Integer cpuFreq) {
        this.cpuFreq = cpuFreq;
    }
    public Integer getCpuTemperature() {
        return cpuTemperature;
    }
    public void setCpuTemperature(Integer cpuTemperature) {
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
    public Integer getIncrCity() {
        return incrCity;
    }
    public void setIncrCity(Integer incrCity) {
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
    public void setStatContents(List<TakeInfoStatContent> statContents) { this.statContents = statContents; }
    public String getLogPath() { return logPath; }
    public void setLogPath(String logPath) { this.logPath = logPath; }
    public String getDailyRebootTime() { return dailyRebootTime; }
    public void setDailyRebootTime(String dailyRebootTime) { this.dailyRebootTime = dailyRebootTime; }
    public String getCritCpuLoad() { return critCpuLoad; }
    public void setCritCpuLoad(String critCpuLoad) { this.critCpuLoad = critCpuLoad; }
    public String getCritRamFree() { return critRamFree; }
    public void setCritRamFree(String critRamFree) { this.critRamFree = critRamFree; }
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeInt(serial);
        dest.writeString(followServer);
        dest.writeInt(replyInterval);
        dest.writeBoolean(hasPing);
        dest.writeString(systemTime);
        dest.writeString(uptime);
        dest.writeString(load1min);
        dest.writeString(load5min);
        dest.writeString(load15min);
        dest.writeInt(cpuFreq);
        dest.writeInt(cpuTemperature);
        dest.writeInt(freeRam);
        dest.writeString(bleMac);
        dest.writeInt(blePidHci0);
        dest.writeInt(bleIntervalMin);
        dest.writeInt(bleIntervalMax);
        dest.writeInt(bleScanIntervalScan);
        dest.writeInt(bleScanIntervalWindow);
        dest.writeParcelableList(interfaces, 0);
        dest.writeString(scUartVer);
        dest.writeInt(scUartPid);
        dest.writeString(boardVersion);
        dest.writeString(stmFirmware);
        dest.writeString(stmBootload);
        dest.writeString(coreLinux);
        dest.writeString(compileDate);
        dest.writeInt(incrCity);
        dest.writeString(stopTransLocate);
        dest.writeParcelableList(transpContents, 0);
        dest.writeParcelableList(statContents, 0);
        dest.writeString(logPath);
        dest.writeString(dailyRebootTime);
        dest.writeString(critCpuLoad);
        dest.writeString(critRamFree);
        dest.writeList(crontabTasks);
        dest.writeString(lastReboot);
    }

    public static final Parcelable.Creator<TakeInfoFull> CREATOR = new Parcelable.Creator<TakeInfoFull>() {
        // распаковываем объект из Parcel
        public TakeInfoFull createFromParcel(Parcel in) {
            return new TakeInfoFull(in);
        }
        public TakeInfoFull[] newArray(int size) {
            return new TakeInfoFull[size];
        }
    };

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

    public TakeInfoFull(){}

    // конструктор, считывающий данные из Parcel
    private TakeInfoFull(Parcel parcel) {
        type = parcel.readString();
        serial = parcel.readInt();
        followServer = parcel.readString();
        replyInterval = parcel.readInt();
        hasPing = parcel.readBoolean();
        systemTime = parcel.readString();
        uptime = parcel.readString();
        load1min = parcel.readString();
        load5min = parcel.readString();
        load15min = parcel.readString();
        cpuFreq = parcel.readInt();
        cpuTemperature = parcel.readInt();
        freeRam = parcel.readInt();
        bleMac = parcel.readString();
        blePidHci0 = parcel.readInt();
        bleIntervalMin = parcel.readInt();
        bleIntervalMax = parcel.readInt();
        bleScanIntervalScan = parcel.readInt();
        bleScanIntervalWindow = parcel.readInt();
        interfaces = parcel.readParcelableList(new ArrayList(), TakeInfoInterface.class.getClassLoader());
        scUartVer = parcel.readString();
        scUartPid = parcel.readInt();;
        boardVersion = parcel.readString();
        stmFirmware = parcel.readString();
        stmBootload = parcel.readString();
        coreLinux= parcel.readString();
        compileDate = parcel.readString();
        incrCity = parcel.readInt();;
        transpContents = parcel.readParcelableList(new ArrayList<>(), TakeInfoTranspContent.class.getClassLoader());
        statContents = parcel.readParcelableList(new ArrayList<>(), TakeInfoStatContent.class.getClassLoader());
        logPath = parcel.readString();
        dailyRebootTime = parcel.readString();
        critCpuLoad = parcel.readString();;
        critRamFree = parcel.readString();
        crontabTasks = parcel.readArrayList(ArrayList.class.getClassLoader());
        lastReboot = parcel.readString();
    }
}


//try: 1.15, props: {"type":"","serial":"","followServer":"","replyInterval":"","hasPing":"false","systemTime":"14:39:58","uptime":"52","load1min":"0.05","load5min":"0.08","load15min":"0.08","cpuFreq":"700","cpuTemperature":"31","freeRam":"312128","bleMac":"b8:27:eb:c6:26:40","blePidHci0":"111","bleIntervalMin":"160","bleIntervalMax":"160","bleScanIntervalScan":"96","bleScanIntervalWindow":"32","phpVer":"0.1.15","scUartVer":"1.6.0.9-debug","scUartPid":"289","boardVersion":"","stmFirmware":"","stmBootload":"","coreLinux":"stp-2021.08.2","compileDate":"2021-11-11 12:02:19","logPath":"","dailyRebootTime":"","critCpuLoad":"","critRamFree":"","crontabTasks":["* * * * * \/usr\/local\/bin\/log_script.sh","* * * * * \/usr\/local\/bin\/wifiPriority.sh",""],"lastReboot":"2021-08-30_21:13:02 - Restarted system at 12 hours of work"}
//exception: java.lang.NumberFormatException: empty String, ip: 192.168.238.155
//try: 1.15, props: {"type":"stopTrans","serial":"7706","followServer":"95.161.142.79","replyInterval":"30","hasPing":"true","systemTime":"14:39:58","uptime":"1:52","load1min":"0.12","load5min":"0.09","load15min":"0.03","cpuFreq":"700","cpuTemperature":"35","freeRam":"310836","bleMac":"b8:27:eb:ad:8f:23","blePidHci0":"111","bleIntervalMin":"160","bleIntervalMax":"160","bleScanIntervalScan":"96","bleScanIntervalWindow":"32","phpVer":"0.1.15","scUartVer":"1.6.0.9-debug","scUartPid":"290","boardVersion":"3.2","stmFirmware":"5.49","stmBootload":"3.4","coreLinux":"stp-2021.08.2","compileDate":"2021-11-11 12:02:19","logPath":"","dailyRebootTime":"","critCpuLoad":"","critRamFree":"","crontabTasks":["* * * * * \/usr\/local\/bin\/log_script.sh","* * * * * \/usr\/local\/bin\/wifiPriority.sh",""],"lastReboot":""}