package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
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
                return new ConfigTransportRvAdapter(context, utils, objects);
            case CONFIG_STATION:
                return new ConfigStationRvAdapter(context, utils, objects);
            case STM_LOG:
                return new StmLogRvAdapter(context, utils, objects);
            case SETTINGS_WIFI:
                return new SettingsWifiRvAdapter(context, utils, objects);
            case SETTINGS_NETWORK:
                return new SettingsNetworkRvAdapter(context, utils, objects);
            case SETTINGS_SCUART:
                return new SettingsScuartRvAdapter(context, utils, objects);
            case SETTINGS_UPDATE_PHP:
                return new SettingsUpdatePhpRvAdapter(context, utils, objects);
            case SETTINGS_UPDATE_CORE:
                return new SettingsUpdateCoreRvAdapter(context, utils, objects);
        }
        return null;
    }
}