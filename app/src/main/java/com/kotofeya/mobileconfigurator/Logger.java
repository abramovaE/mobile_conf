package com.kotofeya.mobileconfigurator;

import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {

    private static final String TAG = "MobileConfigurator";
//    private static List<String> serviceLog = new CopyOnWriteArrayList<>();

    private static final String LOG_FILE = "mc_logReport.log";

    public static final int MAIN_LOG = 0;
    public static final int BASIC_SCANNER_LOG = 1;
    public static final int BLE_SCANNER_LOG = 2;
    public static final int BT_HANDLER_LOG = 3;
    public static final int UTILS_LOG = 4;
    public static final int FILTER_LOG = 5;
    public static final int SCANNER_ADAPTER_LOG = 6;

    public static final int SSH_CONNECTION_LOG = 8;
    public static final int DOWNLOAD_LOG = 9;
    public static final int STATION_CONTEN_LOG = 10;
    public static final int TRANSPORT_CONTENT_LOG = 11;
    public static final int CONTENT_LOG = 12;
    public static final int WIFI_LOG = 13;
    public static final int UPDATE_CONTENT_LOG = 14;
    public static final int APP_LOG = 15;
    public static final int UPDATE_STM_LOG = 16;
    public static final int CONFIG_LOG = 17;
    public static final int UPDATE_LOG = 18;
    public static final int INTERNET_CONN_LOG = 19;
    public static final int TRANSPORT_TRANSIVER_LOG = 20;
    public static final int CHECK_USER_LOG = 21;
    public static final int VIEW_MODEL_LOG = 22;
    public static final int POST_INFO_LOG = 23;
    public static final int FRAGMENT_LOG = 24;
    public static final int STM_LOG_LOG = 25;
    public static final int TRANSIVER_STM_LOG_LOG = 26;
    public static final int UPDATE_CORE_LOG = 27;




    private static final  SparseArray<String> mType = new SparseArray<>();

    static {
        mType.put(MAIN_LOG,"Main_task");
        mType.put(BASIC_SCANNER_LOG, "Basic_scanner_task");
        mType.put(BLE_SCANNER_LOG, "Ble_scanner_task");
        mType.put(BT_HANDLER_LOG, "BT_handler_task");
        mType.put(UTILS_LOG, "Utils_task");
        mType.put(FILTER_LOG, "Filter_task");
        mType.put(SCANNER_ADAPTER_LOG, "Scanner_adapter_task");
//        mType.put(UPDATE_OS_LOG, "Update_os_task");
        mType.put(SSH_CONNECTION_LOG,"Ssh_connection_task");
        mType.put(DOWNLOAD_LOG,"Download_task");
        mType.put(STATION_CONTEN_LOG, "Station_content_task");
        mType.put(TRANSPORT_CONTENT_LOG, "Transport_content_task");
        mType.put(CONTENT_LOG, "Content_task");
        mType.put(WIFI_LOG, "Wifi_task");
        mType.put(UPDATE_CONTENT_LOG, "Update_content_task");
        mType.put(APP_LOG, "App_task");
        mType.put(UPDATE_STM_LOG, "Update_stm_task");
        mType.put(CONFIG_LOG, "Config_task");
        mType.put(UPDATE_LOG, "Update_task");
        mType.put(INTERNET_CONN_LOG, "Internet_connection_task");
        mType.put(TRANSPORT_TRANSIVER_LOG, "Transport_transiver_task");
        mType.put(CHECK_USER_LOG, "Check_user_task");
        mType.put(VIEW_MODEL_LOG, "View_model_task");
        mType.put(POST_INFO_LOG, "Post_info_task");
        mType.put(FRAGMENT_LOG, "Fragment_task");
        mType.put(STM_LOG_LOG, "Stm_log_task");
        mType.put(TRANSIVER_STM_LOG_LOG, "Transiver_stm_log_task");
        mType.put(UPDATE_CORE_LOG, "Update_core_log_task");

    }

    public static void d(int type, String message){
        if (BuildConfig.DEBUG) {
            Log.d(TAG + "|| " + mType.get(type), message);
        }
//        serviceLog.add(mType.get(type) + ": " + message);
        appendLog(mType.get(type) + ": " + message);

    }


    public static void d(String type, String message){
        if (BuildConfig.DEBUG) {
            Log.d(type, message);
        }
        appendLog(type + ": " + message);
    }


    public static void e(int type, String message, Throwable throwable){
        if (BuildConfig.DEBUG) {
            Log.e(TAG + "|| " + mType.get(type), message, throwable);
        }
//        serviceLog.add(mType.get(type) + ": " + message);
        appendLog(mType.get(type) + ": " + message);
    }

    public static String getServiceLogString(){
        StringBuilder sb = new StringBuilder("mobile configurator log\n");
        File file = getLogFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String s;
            while ((s = reader.readLine()) != null){
                sb.append(s);
                sb.append("\n");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void clearLogReport(){
        createLog();
    }


    private static File getLogFile() {
        return new File(App.get().getCacheDir(), LOG_FILE);
    }

    private static void createLog() {
        File file = getLogFile();
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
            appendLog("Created at " + new Date().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendLog(String line) {
        File file = getLogFile();
        if (!file.exists()) createLog();
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.write(line);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}