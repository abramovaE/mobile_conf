package com.kotofeya.mobileconfigurator.domain.transceiver;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoStatContent;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoTranspContent;

import java.util.ArrayList;
import java.util.List;

public class Transceiver {

    private static final String TAG = Transceiver.class.getSimpleName();

    public static final String VERSION_NEW = "new";
    public static final String VERSION_OLD = "old";
    public static final String VERSION_PRE = "pre";
    public static final String TYPE_TRANSPORT = "transport";
    public static final String TYPE_STATIONARY = "stationary";
    public static final String VERSION_UNDEFINED = "0.0.0";

    protected String version;

    private String ssid;
    private String ip;
    private String macWifi;
    private String macBt;
    private String boardVersion;
    private String osVersion;
    private String stmFirmware;
    private String stmBootloader;
    private String core;
    private String modem;
    private String incrementOfContent;
    private String uptime;
    private String cpuTemp;
    private String load;
    private String tType;

    private TakeInfoFull takeInfoFull;

    private String updatingTime;

    private Thread updatingTimer;

    public Thread getUpdatingTimer() {
        return updatingTimer;
    }

    public void setUpdatingTimer(Thread updatingTimer) {
        this.updatingTimer = updatingTimer;
    }

    public Transceiver(String ip, String version, TakeInfoFull takeInfoFull){
        this.ip = ip;
        this.version = version;
        this.takeInfoFull = takeInfoFull;
    }

    public Transceiver(String takeInfo) {
        String[] info = takeInfo.split("\n");
        this.version = VERSION_UNDEFINED;
        this.ssid = info[1].trim();
        this.ip = info[2].trim();
        this.macWifi = info[3].trim();
        this.macBt = info[4].trim();
        this.boardVersion = info[5].trim();
        this.osVersion = info[6].trim();
        this.stmFirmware = info[7].trim();
        this.stmBootloader = info[8].trim();
        this.core = info[9].trim();
        this.modem = info[10].trim();
        this.incrementOfContent = info[11].trim();
        this.uptime = info[12].trim();
        this.cpuTemp = info[13].trim();
        this.load = info[14].trim();
        this.tType = info[17].trim();
    }

    public String getSsid() {
        if(takeInfoFull != null){
            ssid = takeInfoFull.getSerial() + "";
        }
        return formatSsid(ssid);
    }

    public String getIp() {
        return ip;
    }

    public String getOsVersion() {
        return (takeInfoFull != null) ? takeInfoFull.getScUartVer() : osVersion;
    }

    public String getCore() {
        return (takeInfoFull != null) ? takeInfoFull.getCoreLinux() : core;
    }

    public String getModem() {
        return modem;
    }

    public String getIncrementOfContent() {
        if(takeInfoFull != null) {
//            Integer incr = takeInfoFull.getIncrCity();
//            return incr == null ? "none" :  incr + "";
        }
        return incrementOfContent;
    }

    public String getStmFirmware() {
        return (takeInfoFull != null) ? takeInfoFull.getStmFirmware() : stmFirmware;
    }

    public String getStmBootloader() {
        return (takeInfoFull != null) ? takeInfoFull.getStmBootload() : stmBootloader;
    }

    public List<TakeInfoTranspContent> getTranspContents(){
        if(takeInfoFull != null){
            return takeInfoFull.getTranspContents();
        }
        return new ArrayList<>();
    }

    public List<TakeInfoStatContent> getStatContents(){
        if(takeInfoFull != null){
            return takeInfoFull.getStatContents();
        }
        return new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Transceiver: " +
                "ssid=" + getSsid() + ", " +
                "ip=" + getIp() + ", version=" + getVersion() +
                ", tType: " + tType +
                ", takeInfoType: " + takeInfoFull;
    }

    public boolean isStationary() {
        boolean isStationary = (takeInfoFull != null) ? takeInfoFull.getType().equalsIgnoreCase(TYPE_STATIONARY) : false;
        Logger.d(TAG, "isStationary(): " + isStationary);
        return isStationary;
    }

    public boolean isTransport(){
        boolean isTransport = (takeInfoFull != null) ? takeInfoFull.getType().equalsIgnoreCase(TYPE_TRANSPORT) : false;
        Logger.d(TAG, "isTransport(): " + isTransport);
        return isTransport;
    }

    public String getContent(){
        List<TakeInfoTranspContent> transpContents = getTranspContents();
        List<TakeInfoStatContent> statContents = getStatContents();

        StringBuilder sb = new StringBuilder();

        if(isTransport() && transpContents != null) {
            transpContents.forEach(it -> {
                sb.append(it.getLocalRouteList())
                        .append(" - ")
                        .append(it.getIncrRouteList())
                        .append("\n");
            });
        } else if(isStationary() && statContents != null){
            statContents.forEach(it-> {
                    sb.append(it.describeContents())
                            .append(" - ")
                            .append(it.getShortInfo())
                            .append("\n");});
        }
        return sb.toString();
    }


    public String getExpBasicScanInfo(){
        Logger.d(Logger.MAIN_LOG, "getExpBasicScanInfo(), " +
                "transceiver: " + ssid +
                ", takeInfo: " + takeInfoFull);
        if(takeInfoFull != null){
            return takeInfoFull.toString();
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append("ssid: ");
            sb.append(ssid);
            sb.append("\n");
            sb.append("ip: ");
            sb.append(ip);
            sb.append("\n");
            sb.append("mac wifi: ");
            sb.append(macWifi);
            sb.append("\n");
            sb.append("mac bt: ");
            sb.append(macBt);
            sb.append("\n");
            sb.append("board version: ");
            sb.append(boardVersion);
            sb.append("\n");
            sb.append("os version: ");
            sb.append(osVersion);
            sb.append("\n");
            sb.append("stm firmware: ");
            sb.append(stmFirmware);
            sb.append("\n");
            sb.append("stm bootloader: ");
            sb.append(stmBootloader);
            sb.append("\n");
            sb.append("core: ");
            sb.append(core);
            sb.append("\n");
            sb.append("modem: ");
            sb.append(modem);
            sb.append("\n");
            sb.append("increment of content: ");
            sb.append(incrementOfContent);
            sb.append("\n");
            sb.append("uptime: ");
            sb.append(uptime);
            sb.append("\n");
            sb.append("cpu temp: ");
            sb.append(cpuTemp);
            sb.append("\n");
            sb.append("load: ");
            sb.append(load);
            sb.append("\n");
            sb.append("type: ");
            sb.append(tType);
            sb.append("\n");
            return sb.toString();
        }
    }

    public String getTType() {
        String tType = (takeInfoFull != null) ? takeInfoFull.getType() : "";
        if(tType.isEmpty()){
            tType = isTransport() ? TYPE_TRANSPORT : isStationary() ? TYPE_STATIONARY : "";
        }
        return tType;
    }


    public static String formatSsid(String ssid){
        if(ssid != null && ssid.startsWith("stp")) {
            ssid = Integer.parseInt(ssid.replace("stp", "")) + "";
        }
        return ssid;
    }

    public String getUpdatingTime() {
        return updatingTime;
    }
    public void setUpdatingTime(String updatingTime) {
        this.updatingTime = updatingTime;
    }

    public String getVersionString(){
        return version + " " + (version.equals(VERSION_UNDEFINED) ? VERSION_OLD : VERSION_NEW);
    }
}