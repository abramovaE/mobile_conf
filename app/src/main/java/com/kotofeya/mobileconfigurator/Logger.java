package com.kotofeya.mobileconfigurator;

import android.util.Log;
import android.util.SparseArray;

public class Logger {

    private static final String TAG = "MobileConfigurator";

    public static final int MAIN_LOG = 0;
    public static final int BASIC_SCANNER_LOG = 1;
    public static final int BLE_SCANNER_LOG = 2;



//    public static final int BT_LOG = 2;
//    public static final int WIFI_LOG = 3;
//    public static final int DOWNLOAD_LOG = 4;
//    public static final int TALKER_LOG = 5;
//    public static final int FRAG_LOG = 6;
//    public static final int OTHER_LOG = 7;
//    public static final int SHARED_PREFS_LOG = 8;
//    public static final int DB_LOG = 9;
//    public static final int UTILS_LOG = 10;
//    public static final int SETTINGS_LOG = 11;
//    public static final int DIFF_UTILS_LOG = 12;
//    public static final int PLAYER_LOG = 13;




    private static final  SparseArray<String> mType = new SparseArray<>();



    static {
        mType.put(MAIN_LOG,"Main_task");
//        mType.put(SC_LOG,"SC_task");
//        mType.put(BT_LOG,"BT_task");
//        mType.put(WIFI_LOG,"Wifi_task");
//        mType.put(DOWNLOAD_LOG,"Download_task");
//        mType.put(TALKER_LOG,"Talker_task");
//        mType.put(FRAG_LOG, "Fragment_task");
//        mType.put(OTHER_LOG,"Other_tasks");
//        mType.put(SHARED_PREFS_LOG, "SharedUtils_task");
//        mType.put(DB_LOG, "Database_task");
//        mType.put(UTILS_LOG, "Utils_task");
//        mType.put(SETTINGS_LOG, "Settings_task");
//        mType.put(DIFF_UTILS_LOG, "MyDiffUtils_task");
//        mType.put(PLAYER_LOG, "Player_task");

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
