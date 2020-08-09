package com.kotofeya.mobileconfigurator.transivers;

import android.bluetooth.le.ScanResult;

import com.kotofeya.mobileconfigurator.Utils;

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


//    private String version;
    private String basicScanInfo;







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

    public Transiver(String ip, String result){
        String[] info = result.split("\n");
        this.ip = info[2].trim();
        this.ssid = info[1].trim();
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


    public String getSsid() {
        return ssid;
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
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getCore() {
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

//    public String getVersion() {
//        return version;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }

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
                "ip='" + ip + '\'' +

                '}';
    }


    public boolean isStationary() {
        if(rawData != null){
            if(rawData.length == 3 && (rawData[0]&0xff) == Utils.STAT_RADIO_TYPE){
                return true;
            }
            else if(rawData.length == 22 && (rawData[5]&0xff) == Utils.STAT_RADIO_TYPE){
                return true;
            }
        }
        return false;
    }

    public boolean isTransport(){
        if(rawData != null){
            if(rawData.length == 22 && (rawData[5]&0xff) == Utils.TRANSP_RADIO_TYPE) {
                return true;
            }

            else if ((rawData[0] & 0xff) == Utils.TRANSP_RADIO_TYPE) {
                return true;
            }
        }

        return false;
    }

    public String getExpBasicScanInfo(){
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
        return sb.toString();
    }

}
