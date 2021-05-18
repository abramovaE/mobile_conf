package com.kotofeya.mobileconfigurator.newBleScanner;

import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;
import com.kotofeya.mobileconfigurator.transivers.TriolInformer;

import java.util.Arrays;

public class TransiversFactory {

    public Transiver getInformer(CustomScanResult result){
        String name = result.getScanResult().getScanRecord().getDeviceName();;
        int rssi = result.getScanResult().getRssi();
        byte[] data;

        if (name != null && name.startsWith("stp")) {
            byte[] pack1;
            byte[] pack2 = new byte[0];
            byte[] allBytes = result.getScanResult().getScanRecord().getBytes();

            pack1 = Arrays.copyOfRange(allBytes, 9, 31);
            if (allBytes.length > 35) {
                pack2 = Arrays.copyOfRange(allBytes, 35, allBytes.length);
            }
            data = new byte[pack1.length + pack2.length];
            System.arraycopy(pack1, 0, data, 0, pack1.length);
            System.arraycopy(pack2, 0, data, pack1.length, pack2.length);

            String address = result.getScanResult().getDevice().getAddress();
            String deviceName = result.getScanResult().getScanRecord().getDeviceName();
            if (data == null) {
                return null;
            }
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
