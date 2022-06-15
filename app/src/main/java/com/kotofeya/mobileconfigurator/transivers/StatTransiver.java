package com.kotofeya.mobileconfigurator.transivers;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.R;


public class StatTransiver extends Transiver {

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

    private int cityIndex;
    private int type;
    private int floor;
    public StatTransiver(int rssi, String address, String deviceName, byte[] data){
        super(rssi, address, deviceName, data);
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
}
