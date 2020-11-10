package com.kotofeya.mobileconfigurator.transivers;

import android.bluetooth.le.ScanResult;


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

    public int getGroupId() {
        return ((getRawData()[14] & 0xFF) << 24) + ((getRawData()[15] & 0xFF) << 16) + ((getRawData()[16] & 0xFF) << 8) + (getRawData()[17] & 0xFF);
    }

    public int getFloor() {
        return (getRawData()[18] & 0xFF);
    }

}
