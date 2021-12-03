package com.kotofeya.mobileconfigurator.newBleScanner;

import android.bluetooth.le.ScanResult;

public class CustomScanResult {

    private ScanResult scanResult;
    private long appearanceTime;
    private String bluetoothDeviceMac;

    public CustomScanResult(ScanResult result){
        this.scanResult = result;
        this.appearanceTime = System.currentTimeMillis();
        this.bluetoothDeviceMac = result.getDevice().getAddress();
    }

    public ScanResult getScanResult() {
        return scanResult;
    }
    public long getAppearanceTime() {
        return appearanceTime;
    }

    @Override
    public String toString() {
        return "CustomScanResult: " + scanResult +" " + appearanceTime + " " +
                bluetoothDeviceMac;
    }

    public int getTransiverType(){
        return scanResult.getScanRecord().getBytes()[14] & 0xff;
    }
}
