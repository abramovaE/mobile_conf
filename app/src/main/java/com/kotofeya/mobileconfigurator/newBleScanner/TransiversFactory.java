package com.kotofeya.mobileconfigurator.newBleScanner;

import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;
import com.kotofeya.mobileconfigurator.transivers.TriolInformer;


public class TransiversFactory {

    public Transiver getInformer(CustomScanResult result){
        int rssi = result.getScanResult().getRssi();
        String address = result.getScanResult().getDevice().getAddress();
        String deviceName = result.getScanResult().getScanRecord().getDeviceName();
        byte[] data = CustomBluetooth.getFullData(result.getScanResult());
        if (data != null) {
            switch (result.getTransiverType()) {
                case MySettings.TRANSPORT:
                    return new TransportTransiver(rssi, address, deviceName, data);
                case MySettings.STATIONARY:
                    return new StatTransiver(rssi, address, deviceName, data);
                case MySettings.TRIOL:
                    return new TriolInformer(rssi, address, deviceName, data);
            }
        }
        return null;
    }
}
