package com.kotofeya.mobileconfigurator;

import android.bluetooth.le.ScanResult;

public class Transiver {


    int BUZZER_DISABLED = 3;
    int BUZZER_READY = 0;
    int BUZZER_ON = 1;
    int BUZZER_BUSY = 2;

    int BUZZER_DISABLE_NEW = 0b00;
    int BUZZER_READY_NEW = 0b01;
    int BUZZER_ON_NEW = 0b10;
    int BUZZER_BUSY_NEW = 0b11;




    private String ssid;
    private String version;
    private String stmFirmware;
    private String stmBootloader;
    private String basicScanInfo;

    private String uptime;
    private String ip;



    private String address;
    private int rssi;
    private byte[] rawData;
//    private String addInfo;
    private boolean delFlag;
    private int delCount;
//    private int sayCounter;
//    private String content;

    private int transVersion;
    int VERSION_OLD = 1;
    int VERSION_NEW = 2;

    public Transiver(String ip) {
        this.ip = ip;
    }

    public Transiver(ScanResult result) {
        rssi = result.getRssi();
        address = result.getDevice().getAddress();
        rawData = result.getScanRecord().getManufacturerSpecificData(0xffff);

        if(result.getScanRecord().getDeviceName().equals("stp")){
            int i = (((rawData[2] & 0xFF) << 16) + ((rawData[3] & 0xFF) << 8) + (rawData[4] & 0xFF));
            ssid = String.valueOf(i);
            transVersion = VERSION_NEW;
        }
        else {
            ssid = result.getScanRecord().getDeviceName();
            transVersion = VERSION_OLD;
        }
        delFlag = false;

    }


    public int getTransVersion() {
        return transVersion;
    }

    public void setTransVersion(int transVersion) {
        this.transVersion = transVersion;
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


    public boolean isDelFlag() {
        return delFlag;
    }

    public void setDelFlag(boolean delFlag) {
        this.delFlag = delFlag;
    }

    public int getRssi() {
        return rssi;
    }

    public int getDelCount() {
        return delCount;
    }

    public void setDelCount(int delCount) {
        this.delCount = delCount;
    }

    public byte[] getRawData() {
        return rawData;
    }
    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
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
        return "Transiver{" +
                "ssid='" + ssid + '\'' +
                '}';
    }


    public boolean isStationary() {
        if(rawData.length == 3 && (rawData[0]&0xff) == Utils.STAT_RADIO_TYPE){
            return true;
        }
        else if(rawData.length == 22 && (rawData[5]&0xff) == Utils.STAT_RADIO_TYPE){
            return true;
        }
        return false;
    }

    public boolean isTransport(){
        if(rawData.length == 22 && (rawData[5]&0xff) == Utils.TRANSP_RADIO_TYPE) {
            return true;
        }

        else if ((rawData[0] & 0xff) == Utils.TRANSP_RADIO_TYPE) {
            return true;
        }
        return false;
    }
}
