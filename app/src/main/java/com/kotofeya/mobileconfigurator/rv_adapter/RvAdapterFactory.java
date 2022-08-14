package com.kotofeya.mobileconfigurator.rv_adapter;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.util.List;

public class RvAdapterFactory {
    public static RvAdapter getRvAdapter(RvAdapterType adapterType,
                                         List<Transceiver> objects,
                                         AdapterListener adapterListener){
        switch (adapterType){
            case BASIC_SCANNER_TYPE:
                return new BasicScannerRvAdapter(objects);
            case BLE_SCANNER_TYPE:
                return new BleScannerRvAdapter(objects);
            case UPDATE_OS_TYPE:
            case UPDATE_STM_TYPE:
            case UPDATE_CONTENT_TYPE:
            case CONFIG_TRANSPORT:
            case CONFIG_STATION:
            case SETTINGS:
            case SETTINGS_UPDATE_CORE:
            case SETTINGS_UPDATE_PHP:
                return new RvAdapter(objects, adapterListener, adapterType);
        }
        return null;
    }
}