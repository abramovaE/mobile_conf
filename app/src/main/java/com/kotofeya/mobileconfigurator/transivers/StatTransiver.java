package com.kotofeya.mobileconfigurator.transivers;

import android.bluetooth.le.ScanResult;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.R;


public class StatTransiver extends Transiver {

    public StatTransiver(ScanResult result) {
        super(result);
    }

    public StatTransiver(String ssid, String ip, String macWifi, String macBt, String boardVersion, String osVersion,
                              String stmFirmware, String stmBootloader, String core, String modem, String incrementOfContent,
                              String uptime, String cpuTemp, String load, String tType) {
        super(ssid, ip, macWifi, macBt, boardVersion, osVersion,
            stmFirmware, stmBootloader, core, modem, incrementOfContent,
            uptime, cpuTemp, load, tType);
    }
    @Override
    String getFirstPartExpInfo() {
        return "";
    }

    @Override
    String getSecondPartExpInfo() {
        StringBuilder text = new StringBuilder();
        text.append(getType());
        text.append(" ");
        text.append(getGroupId());
        return text.toString();
    }

    public int getType() {
        return ((getRawData()[12] & 0xFF) << 8) + (getRawData()[13] & 0xFF);
    }

    public String getStringType(){
        return App.get().getResources().getStringArray(R.array.stationars)[getType()];
    }

    public int getGroupId() {
        return ((getRawData()[14] & 0xFF) << 24) + ((getRawData()[15] & 0xFF) << 16) + ((getRawData()[16] & 0xFF) << 8) + (getRawData()[17] & 0xFF);
    }

    public int getFloor() {
        return (getRawData()[18] & 0xFF);
    }

    private String[] point0;
    private String[] point1;
    private String[] point2;
    private String[] point3;
    private String[] point4;
    private String[][] sPoints;
//    private Stat stat;

    private int cityIndex;
    private int type;
    private int floor;
    public StatTransiver(int rssi, String address, String deviceName, byte[] data){
        super(rssi, address, deviceName, data);
        this.point0 = new String[4];
        this.point1 = new String[4];
        this.point2 = new String[4];
        this.point3 = new String[4];
        this.point4 = new String[4];
        setCityIndex();
        setType();
        setFloor();
//        setStat();
    }


    public void updateTransiver(int rssi, String address, String deviceName, byte[] data){
        super.rssi = rssi;
        this.address = address;
        setRawData(data);
        super.setSSidAndVersion(deviceName);
        super.setBtPackVersion();
        super.setTransiverType();
        super.setIncrement();
        super.setCrc();
        this.point0 = new String[4];
        this.point1 = new String[4];
        this.point2 = new String[4];
        this.point3 = new String[4];
        this.point4 = new String[4];
        setCityIndex();
        setType();
        setFloor();
    }

    private void setCityIndex(){
        if(super.btPackVersion > 0){
            int city = (((rawData[12] & 0xff)) + (rawData[13] & 0xff));
            if(city == 0){
                city = 2;
            }
            this.cityIndex = city;
        }
        else {
            this.cityIndex = 2;
        }
    }
    private void setType(){
        if(super.btPackVersion == 0) {
            this.type =  ((rawData[12] & 0xFF) << 8) + (rawData[13] & 0xFF);
        }
        else if(super.btPackVersion > 0) {
            this.type = ((rawData[14] & 0xFF) << 8) + (rawData[15] & 0xFF);
        }
        else {
            this.type = 0;
        }
    }
    private void setFloor(){
        if(super.btPackVersion == 0) {
            this.floor =  (rawData[18] & 0xFF);
        }
        else if(super.btPackVersion > 0){
            this.floor = (rawData[16] & 0xFF);
        }
        else {
            this.floor = 0;
        }
    }
//    private void setStat(){
//        this.stat = App.get().getDBHelper().readStat(getSsid(), App.get().getLanguage());
//        if(this.stat != null){
//            setPoint0(this.stat.getPoint0());
//            setPoint1(this.stat.getPoint1());
//            setPoint2(this.stat.getPoint2());
//            setPoint3(this.stat.getPoint3());
//            setPoint4(this.stat.getPoint4());
//        }
//    }
}
