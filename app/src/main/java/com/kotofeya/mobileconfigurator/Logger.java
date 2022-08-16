package com.kotofeya.mobileconfigurator;

import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {

    private static final String TAG = "MobileConfigurator";
    private static final String LOG_FILE = "mc_logReport.log";

    public static final int MAIN_LOG = 0;
    public static final int BASIC_SCANNER_LOG = 1;
    public static final int SCANNER_ADAPTER_LOG = 6;
    public static final int STATION_CONTENT_LOG = 10;
    public static final int APP_LOG = 15;
    public static final int UPDATE_STM_LOG = 16;
    public static final int INTERNET_CONN_LOG = 19;
    public static final int CHECK_USER_LOG = 21;
    public static final int STM_LOG_LOG = 25;

    private static final  SparseArray<String> mType = new SparseArray<>();

    static {
        mType.put(MAIN_LOG,"Main_task");
        mType.put(BASIC_SCANNER_LOG, "Basic_scanner_task");
        mType.put(SCANNER_ADAPTER_LOG, "Scanner_adapter_task");
        mType.put(STATION_CONTENT_LOG, "Station_content_task");
        mType.put(APP_LOG, "App_task");
        mType.put(UPDATE_STM_LOG, "Update_stm_task");
        mType.put(INTERNET_CONN_LOG, "Internet_connection_task");
        mType.put(CHECK_USER_LOG, "Check_user_task");
        mType.put(STM_LOG_LOG, "Stm_log_task");
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
            appendLog("Created at " + new Date());
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