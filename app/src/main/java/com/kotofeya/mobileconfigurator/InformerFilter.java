package com.kotofeya.mobileconfigurator;

import android.bluetooth.le.ScanResult;

import static com.kotofeya.mobileconfigurator.Logger.FILTER_LOG;


public class InformerFilter {
    private Utils utils;
    InformerFilter(Utils utils){
        this.utils = utils;
    }

    public boolean filter(ScanResult result) {
        String name = result.getScanRecord().getDeviceName();
        if (name != null && name.startsWith("stp")){
            byte[] data = result.getScanRecord().getManufacturerSpecificData(0xFFFF);
            if (data == null){
                return false;
            }
            if (utils.getRadioType() == Utils.TRANSP_RADIO_TYPE){
                return filterTransport(data);
            } else if (utils.getRadioType() == Utils.STAT_RADIO_TYPE){
                return filterStationary(data);
            } else if (utils.getRadioType() == Utils.ALL_RADIO_TYPE){
                return true;
            }
        }
        return false;
    }

    private boolean filterStationary(byte[] data) {
        if(data.length == 3 && ((data[0]&0xff) == Utils.STAT_RADIO_TYPE)){
            return true;
        }
        else if(data.length == 22 && ((data[5]&0xff) == Utils.STAT_RADIO_TYPE)){
            return true;
        }
        return false;
    }

    private boolean filterTransport(byte[] data) {
        if (data.length == 22 && ((data[5] & 0xff) == Utils.TRANSP_RADIO_TYPE)) {
            return true;
        } else {
            if ((data[0] & 0xff) == Utils.TRANSP_RADIO_TYPE) {
                return true;
            }
            return false;
        }
    }
}