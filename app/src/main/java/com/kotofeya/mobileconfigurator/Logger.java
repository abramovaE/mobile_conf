package com.kotofeya.mobileconfigurator;

import android.util.Log;
import android.util.SparseArray;

public class Logger {

    private static final String TAG = "MobileConfigurator";

    public static final int MAIN_LOG = 0;
    public static final int BASIC_SCANNER_LOG = 1;
    public static final int BLE_SCANNER_LOG = 2;
    public static final int BT_HANDLER_LOG = 3;
    public static final int UTILS_LOG = 4;
    public static final int FILTER_LOG = 5;
    public static final int SCANNER_ADAPTER_LOG = 6;
    public static final int UPDATE_OS_LOG = 7;
    public static final int SSH_CONNECTION_LOG = 8;




    private static final  SparseArray<String> mType = new SparseArray<>();



    static {
        mType.put(MAIN_LOG,"Main_task");
        mType.put(BASIC_SCANNER_LOG, "Basic_scanner_task");
        mType.put(BLE_SCANNER_LOG, "Ble_scanner_task");
        mType.put(BT_HANDLER_LOG, "BT_handler_task");
        mType.put(UTILS_LOG, "Utils_task");
        mType.put(FILTER_LOG, "Filter_task");
        mType.put(SCANNER_ADAPTER_LOG, "Scanner_adapter_task");
        mType.put(UPDATE_OS_LOG, "Update_os_task");
        mType.put(SSH_CONNECTION_LOG,"Ssh_connection_task");


    }

    public static void d(int type, String message){
        if (BuildConfig.DEBUG) {
            Log.d(TAG + "|| " + mType.get(type), message);
        }
    }
    public static void e(int type, String message, Throwable throwable){
        if (BuildConfig.DEBUG) {
            Log.e(TAG + "|| " + mType.get(type), message, throwable);
        }
    }
}
