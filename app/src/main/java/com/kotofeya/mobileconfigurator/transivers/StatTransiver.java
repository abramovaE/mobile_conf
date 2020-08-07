package com.kotofeya.mobileconfigurator.transivers;

import android.bluetooth.le.ScanResult;


public class StatTransiver extends Transiver {
    public StatTransiver(String ip) {
        super(ip);
    }

    public StatTransiver(ScanResult result) {
        super(result);
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
