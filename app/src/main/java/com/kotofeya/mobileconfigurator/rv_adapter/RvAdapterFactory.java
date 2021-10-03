package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;

import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class RvAdapterFactory {
    public static RvAdapter getRvAdapter(Context context, Utils utils, RvAdapterType adapterType, List<Transiver> objects){
        switch (adapterType){
            case BASIC_SCANNER_TYPE:
                return new BasicScannerRvAdapter(context, utils, objects);
            case BLE_SCANNER_TYPE:
                return new BleScannerRvAdapter(context, utils, objects);
            case UPDATE_OS_TYPE:
                return new UpdateOsRvAdapter(context, utils, objects);
            case UPDATE_STM_TYPE:
                return new UpdateStmRvAdapter(context, utils, objects);
            case UPDATE_CONTENT_TYPE:
                return new UpdateContentRvAdapter(context, utils, objects);
            case CONFIG_TRANSPORT:
                return new ConfigTransportRvAdapter(context, utils, objects, getFragmentTag(adapterType));
            case CONFIG_STATION:
                return new ConfigStationRvAdapter(context, utils, objects, getFragmentTag(adapterType));
            case SETTINGS_UPDATE_PHP:
                return new SettingsUpdatePhpRvAdapter(context, utils, objects);
            case SETTINGS_UPDATE_CORE:
                return new SettingsUpdateCoreRvAdapter(context, utils, objects);
            case STM_LOG:
            case SETTINGS_WIFI:
            case SETTINGS_NETWORK:
            case SETTINGS_SCUART:
                return new SettingsRvAdapter(context, utils, objects, getFragmentTag(adapterType));
        }
        return null;
    }

    private static String getFragmentTag(RvAdapterType adapterType){
        switch (adapterType){
            case BASIC_SCANNER_TYPE:
            case BLE_SCANNER_TYPE:
            case UPDATE_OS_TYPE:
            case UPDATE_STM_TYPE:
            case UPDATE_CONTENT_TYPE:
            case SETTINGS_UPDATE_PHP:
            case SETTINGS_UPDATE_CORE:
                return null;
            case CONFIG_TRANSPORT:
                return FragmentHandler.TRANSPORT_CONTENT_FRAGMENT;
            case CONFIG_STATION:
                return FragmentHandler.STATION_CONTENT_FRAGMENT;
            case STM_LOG:
                return FragmentHandler.TRANSIVER_STM_LOG_FRAGMENT;
            case SETTINGS_WIFI:
                return FragmentHandler.TRANSIVER_SETTINGS_WIFI_FRAGMENT;
            case SETTINGS_NETWORK:
                return FragmentHandler.TRANSIVER_SETTINGS_NETWORK_FRAGMENT;
            case SETTINGS_SCUART:
                return FragmentHandler.TRANSIVER_SETTINGS_SCUART_FRAGMENT;
        }
        return null;
    }
}