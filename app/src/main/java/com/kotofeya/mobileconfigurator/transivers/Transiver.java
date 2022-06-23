package com.kotofeya.mobileconfigurator.transivers;

import android.content.Context;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;

public class Transiver {

    private static final String TAG = Transiver.class.getSimpleName();
    int BUZZER_DISABLED = 3;
    int BUZZER_READY = 0;
    int BUZZER_ON = 1;
    int BUZZER_BUSY = 2;

    int BUZZER_DISABLE_NEW = 0b00;
    int BUZZER_READY_NEW = 0b01;
    int BUZZER_ON_NEW = 0b10;
    int BUZZER_BUSY_NEW = 0b11;

    protected int intVesion;
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

    protected String address;
    protected int rssi;
    protected byte[] rawData;

    private int transVersion;
    public static final int VERSION_OLD = 1;
    public static final int VERSION_NEW = 2;

    private TakeInfoFull takeInfoFull;

    private int increment;
    protected int btPackVersion;
    private int transiverType;

    public Transiver(String ssid, String ip, String macWifi, String macBt, String boardVersion, String osVersion,
                     String stmFirmware, String stmBootloader, String core, String modem, String incrementOfContent,
                     String uptime, String cpuTemp, String load, String tType) {
        this.ssid = ssid;
        this.ip = ip;
        this.macWifi = macWifi;
        this.macBt = macBt;
        this.boardVersion = boardVersion;
        this.osVersion = osVersion;
        this.stmFirmware = stmFirmware;
        this.stmBootloader = stmBootloader;
        this.core = core;
        this.modem = modem;
        this.incrementOfContent = incrementOfContent;
        this.uptime = uptime;
        this.cpuTemp = cpuTemp;
        this.load = load;
        this.tType = tType;
    }

    public Transiver(String ssid, String ip) {
        this.ssid = ssid;
        this.ip = ip;
    }

    public Transiver(String ip){
        this.ip = ip;
    }


    public String getSsid() {
        if(takeInfoFull != null){
            ssid = takeInfoFull.getSerial() + "";
        }
        return formatSsid(ssid);
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacWifi() {

        return macWifi;
    }

    public void setMacWifi(String macWifi) {
        this.macWifi = macWifi;
    }

    public String getMacBt() {
        return macBt;
    }

    public void setMacBt(String macBt) {
        this.macBt = macBt;
    }

    public String getBoardVersion() {
        return boardVersion;
    }

    public void setBoardVersion(String boardVersion) {
        this.boardVersion = boardVersion;
    }

    public String getOsVersion() {
        if(takeInfoFull != null){
            return takeInfoFull.getScUartVer();
        }
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getCore() {
        if(takeInfoFull != null){
            return takeInfoFull.getCoreLinux();
        }
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public String getModem() {
        return modem;
    }

    public void setModem(String modem) {
        this.modem = modem;
    }

    public String getIncrementOfContent() {
        return incrementOfContent;
    }

    public void setIncrementOfContent(String incrementOfContent) {
        this.incrementOfContent = incrementOfContent;
    }

    public String getCpuTemp() {
        return cpuTemp;
    }

    public void setCpuTemp(String cpuTemp) {
        this.cpuTemp = cpuTemp;
    }

    public String getLoad() {
        return load;
    }

    public void setLoad(String load) {
        this.load = load;
    }

    public int getTransVersion() {
        return transVersion;
    }

    public void setTransVersion(int transVersion) {
        this.transVersion = transVersion;
    }

    public String getStmFirmware() {
        if(takeInfoFull != null){
            return takeInfoFull.getStmFirmware();
        }
        return stmFirmware;
    }

    public void setStmFirmware(String stmFirmware) {
        this.stmFirmware = stmFirmware;
    }

    public String getStmBootloader() {
        if(takeInfoFull != null){
            return takeInfoFull.getStmBootload();
        }
        return stmBootloader;
    }

    public void setStmBootloader(String stmBootloader) {
        this.stmBootloader = stmBootloader;
    }


    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    synchronized public boolean isCalled(int BuzzerNumber) {
        if (BuzzerNumber > 4) {
            return false;
        }
        if(transVersion == VERSION_OLD){
            if(this instanceof StatTransiver){
                BuzzerNumber = BuzzerNumber -1;
            }
            return ((rawData[1] >> (BuzzerNumber * 2)) & 0b00000011) == BUZZER_ON;
        }
        else {
            return ((rawData[6] >> (6 - 2 * BuzzerNumber)) & 0b00000011) == BUZZER_ON_NEW;
        }
    }

    public int getBtPackVersion() {
        return rawData[0] & 0xff;
    }

    synchronized public boolean isCallReady(int BuzzerNumber) {
        if (BuzzerNumber > 4) {
            return false;
        }
        if(transVersion == VERSION_OLD){
            int Temp = ((rawData[1] >> (BuzzerNumber * 2)) & 0b00000011);
            return (Temp == BUZZER_READY || Temp == BUZZER_ON);
        }
        else {
            int temp  =  ((rawData[6] >> (6 - 2 * BuzzerNumber)) & 0b00000011);
            return (temp == BUZZER_READY_NEW || temp == BUZZER_ON_NEW);
        }
    }


    public boolean isCallBusy(int BuzzerNumber) {
        if (BuzzerNumber > 4) {
            return false;
        }
        if(transVersion == VERSION_OLD){
            return ((rawData[1] >> (BuzzerNumber * 2)) & 0b00000011) == BUZZER_BUSY;
        }
        else {
            return ((rawData[6] >> (6 - 2 * BuzzerNumber)) & 0b00000011) == BUZZER_BUSY_NEW;
        }
    }


    public int getFloorOrDoorStatus() {
        int Result;
        if(transVersion == VERSION_OLD){
            if((rawData[0] & 0xff) == Utils.STAT_RADIO_TYPE) {
                if((rawData[2] & 0xFF) < 0xEA) {
                    Result = rawData[2] & 0xFF;
                } else {
                    Result = -(0xFF - rawData[2] & 0xFF);
                }
            } else {
                if (rawData.length > 10) {
                    Result = (rawData[7] & 0xff);
                } else {
                    Result = -1;
                }
            }
        }
        else {
            if((rawData[5] & 0xff) == Utils.STAT_RADIO_TYPE) {
                if((rawData[7] & 0xFF) < 0xEA) {
                    Result = rawData[7] & 0xFF;
                } else {
                    Result = -(0xFF - rawData[7] & 0xFF);
                }
            } else {
                Result = ((rawData[7] >> 6) & 0b00000011);
            }
        }

        return Result;
    }

    public String getCrc(){
        if(transVersion == VERSION_NEW) {
            return (byteToHex(rawData[8]) + byteToHex(rawData[9]) + byteToHex(rawData[10]) + byteToHex(rawData[11])).toLowerCase();
        }
        else {
            return null;
        }
    }


    public int getIncrement() {
        if(transVersion == VERSION_OLD){
            return  rawData[8] & 0xff;
        }
        else {
            return rawData[7] & 0b00001111;
        }
    }


    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String byteToHex(byte bytes) {
        char[] hexChar = new char[2];
        int v = bytes & 0xff;
        hexChar[0] = hexArray[v >>> 4];
        hexChar[1] = hexArray[v & 0x0F];
        return new String(hexChar);
    }



    @Override
    public String toString() {
        return "Transiver: " +
                "ssid=" + getSsid() + ", " +
                "ip=" + getIp() + ", version=" + getVersion() + ", type: " + transiverType + ", tType: " + tType + ", takeInfoType: " + takeInfoFull;
    }


    public boolean isStationary() {
        if(takeInfoFull != null){
            return takeInfoFull.getType().equalsIgnoreCase("stationary");
        }
        else {
            if (rawData != null) {
                if (rawData.length == 3 && (rawData[0] & 0xff) == Utils.STAT_RADIO_TYPE) {
                    return true;
                } else if (rawData.length >= 22 && (rawData[5] & 0xff) == Utils.STAT_RADIO_TYPE) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isTransport(){
        if(takeInfoFull != null){
            return takeInfoFull.getType().equalsIgnoreCase("transport");
        } else {
            if(rawData != null){
                if(rawData.length >= 22 && (rawData[5] & 0xff) == Utils.TRANSPORT_RADIO_TYPE) {
                    return true;
                }
                else if ((rawData[0] & 0xff) == Utils.TRANSPORT_RADIO_TYPE) {
                    return true;
                }
            }
            return false;
        }
    }

    public String getExpBasicScanInfo(){
        Logger.d(Logger.MAIN_LOG, "get exp basic scan info, transiver: " + ssid + ", takeinfo: " + takeInfoFull);
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

    public String getBleExpText(){
        StringBuilder text = new StringBuilder();
        text.append("inf state: /ready/busy/called");
        text.append("\n");
        for(int i = 0; i < 4; i++){
            text.append("inf" + i + ": " +  this.isCallReady(i) + "/" + this.isCallBusy(i) + "/" + this.isCalled(i));
            text.append("\n");
        }
        getFirstPartExpInfo();
        text.append("crc: " +  this.getCrc());
        text.append("\n");
        text.append("incr: " + this.getIncrement());
        text.append("\n");
        getSecondPartExpInfo();
        return text.toString();
    }

    String getFirstPartExpInfo(){return "";}
    String getSecondPartExpInfo(){return "";}

    public String getStringDoorStatus(int doorStatus) {
        Context ctx = App.get().getApplicationContext();
        switch (doorStatus) {
            case 0:
                return ctx.getString(R.string.doorsClosed);
            case 1:
                return ctx.getString(R.string.doorsOpened);
            case 2:
                return ctx.getString(R.string.doorsBroken);
            default:
                return null;
        }
    }


    public String getTType() {
        String tType = "";
        if(takeInfoFull != null){
            tType = takeInfoFull.getType();
        }

        if(tType.isEmpty()){
            if(isTransport()){
                tType = "transport";
            }
            else if(isStationary()){
                tType = "stationary";
            }
        }
        return tType;
    }

    public void setTType(String type) {
        this.tType = type;
    }

    public void setTakeInfoFull(TakeInfoFull takeInfoFull) {
        Logger.d(TAG, "set take info full: " + takeInfoFull);
        this.takeInfoFull = takeInfoFull;
    }

    public Transiver(int rssi, String address, String deviceName, byte[] data) {
        this.rssi = rssi;
        this.address = address;
        setRawData(data);
        setSSidAndVersion(deviceName);
        setBtPackVersion();
        setTransiverType();
        setIncrement();
    }

    public int getType(){
        return 0;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }
    protected void setSSidAndVersion(String deviceName){
        if(deviceName.equals("stp")){
            int i = (((rawData[2] & 0xFF) << 16) + ((rawData[3] & 0xFF) << 8) + (rawData[4] & 0xFF));
            this.ssid = i + "";
            this.intVesion = VERSION_NEW;
        }
        else {
            this.ssid = deviceName;
            this.intVesion = VERSION_OLD;
        }
    }




    protected void setBtPackVersion(){ this.btPackVersion = rawData[0] & 0xff;; }
    protected void setTransiverType(){
        this.transiverType = rawData[5] & 0xff;
    }
    protected void setIncrement(){
        if(intVesion == VERSION_OLD){
            this.increment = rawData[8] & 0xff;
        }
        else {
            this.increment = rawData[7] & 0b00001111;
        }
    }

    public int getTransiverType() {
        return this.transiverType;
    }

    public static String formatSsid(String ssid){
        if(ssid != null && ssid.startsWith("stp")) {
            ssid = Integer.parseInt(ssid.replace("stp", "")) + "";
        }
        return ssid;
    }
}
