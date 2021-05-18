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
//        Logger.d(Logger.OTHER_LOG, this.toString());
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public long getAppearanceTime() {
        return appearanceTime;
    }

    public String getBluetoothDeviceMac() {
        return bluetoothDeviceMac;
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
