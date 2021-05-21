package com.kotofeya.mobileconfigurator;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import java.io.File;


//@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.KEY_VALUE_LIST)
//@AcraHttpSender(uri = "http://95.161.210.44/mobile_conf_acra.php",
//        httpMethod = HttpSender.Method.POST)
public class App extends Application {
    private static App instance;
    private Context context;
    private FragmentHandler fragmentHandler;
    private SharedPreferences preferences;
    private String updateOsFilePath;
    private String updateOsFileVersion;
    private boolean showAccessPointDialog;


    private String login;

    private static final String PREF_NAME = "mobile_conf_pref";

    public static App get() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        updateOsFilePath = preferences.getString("updateOsFilePath", "");
        updateOsFileVersion = preferences.getString("updateOsFileVersion", "");
        showAccessPointDialog = preferences.getBoolean("isAskForTeneth", true);

        if(!new File(updateOsFilePath).exists()){
            setUpdateOsFileVersion("");
            setUpdateOsFilePath("");
        }

        Downloader.tempUpdateOsFile = new File(updateOsFilePath);
        Logger.d(Logger.APP_LOG, "updateOsVersion: " + updateOsFileVersion);
        Logger.d(Logger.APP_LOG, "updateOsFilePath: " + updateOsFilePath + ", isExist: " + Downloader.tempUpdateOsFile.exists());
        Logger.d(Logger.APP_LOG, "is_ask_forteneth: " + isAskForTeneth());

//        retrofit = new Retrofit.Builder()
//                .baseUrl("http://95.161.210.44/update/")
//                .build();
    }

    public boolean isAskForTeneth(){
        return preferences.getBoolean("isAskForTeneth", true);

//        return true;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setAskForTeneth(boolean value) {
        Logger.d(Logger.APP_LOG, "set ask fort teneth: " + value);
        preferences.edit().putBoolean("isAskForTeneth", value).commit();
        this.showAccessPointDialog = value;
    }


    public String getUpdateOsFilePath() {
        return updateOsFilePath;
    }

    public void setUpdateOsFilePath(String updateOsFilePath) {
        preferences.edit().putString("updateOsFilePath", updateOsFilePath).commit();
        this.updateOsFilePath = updateOsFilePath;
    }

    public String getUpdateOsFileVersion() {
        return updateOsFileVersion;
    }

    public void setUpdateOsFileVersion(String updateOsFileVersion) {
        preferences.edit().putString("updateOsFileVersion", updateOsFileVersion).commit();
        this.updateOsFileVersion = updateOsFileVersion;
    }

//    public String getLogReport(){
//        return preferences.getString("logReport", "");
//    }
//
//    public void setLogReport(String logReport) {
//        preferences.edit().putString("logReport", logReport).commit();
//    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public FragmentHandler getFragmentHandler() {
        return fragmentHandler;
    }

    public void setFragmentHandler(FragmentHandler fragmentHandler) {
        this.fragmentHandler = fragmentHandler;
    }

}