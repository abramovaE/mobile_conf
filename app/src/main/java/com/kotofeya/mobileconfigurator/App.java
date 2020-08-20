package com.kotofeya.mobileconfigurator;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.io.File;

public class App extends Application {

    private static App instance;

    private Context context;
    private FragmentHandler fragmentHandler;

    private SharedPreferences preferences;
    private String updateOsFilePath;
    private String updateOsFileVersion;

    private static final String PREF_NAME = "mobile_conf_pref";


    public static App get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        updateOsFilePath = preferences.getString("updateOsFilePath", "");
        updateOsFileVersion = preferences.getString("updateOsFileVersion", "");

        if(!new File(updateOsFilePath).exists()){
            setUpdateOsFileVersion("");
            setUpdateOsFilePath("");
        }

        Downloader.tempUpdateOsFile = new File(updateOsFilePath);
        Logger.d(Logger.APP_LOG, "updateOsVersion: " + updateOsFileVersion);
        Logger.d(Logger.APP_LOG, "updateOsFilePath: " + updateOsFilePath + ", isExist: " + Downloader.tempUpdateOsFile.exists());
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