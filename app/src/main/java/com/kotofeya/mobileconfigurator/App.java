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
import java.util.HashSet;
import java.util.Set;

@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraHttpSender(uri = "http://95.161.210.44/mobile_conf_acra.php",
        httpMethod = HttpSender.Method.POST)
public class App extends Application {

    private static App instance;
    private static final String PREF_NAME = "mobile_conf_pref";
    private static final String UPDATE_CONTENT_FILES = "update_content_files";
    private static final String UPDATE_CORE_FILES = "update_core_files_path";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String IS_REMEMBERED = "is_remembered";
    private static final String IS_ASK_FOR_TENETH = "is_ask_for_teneth";
    private static final String UPDATE_OS_FILE_PATH = "update_os_file_path";
    private static final String UPDATE_OS_FILE_VERSION = "update_os_file_version";

    private String login;
    private String level;

    private Context context;
    private FragmentHandler fragmentHandler;
    private SharedPreferences preferences;
    private String updateOsFilePath;
    private String updateOsFileVersion;

    private boolean showAccessPointDialog;
    private File[] updateCoreFilesPath;
    private Set<String> updateContentFilePaths;

    String password;
    boolean isRemembered;


    public Set<String> getUpdateContentFilePaths() {
        Logger.d(Logger.APP_LOG, "getupdatecontentpaths: " + updateContentFilePaths);
        return updateContentFilePaths;
    }
    public void setUpdateContentFilePaths(Set<String> paths) {
        preferences.edit().remove(UPDATE_CONTENT_FILES).commit();
        preferences.edit().putStringSet(UPDATE_CONTENT_FILES, paths).commit();
        this.updateContentFilePaths = paths;
    }

    public void saveUpdateContentFilePaths(String filePath){
        updateContentFilePaths.add(filePath);
        setUpdateContentFilePaths(updateContentFilePaths);
    }

    public File[] getUpdateCoreFilesPath() {
        return updateCoreFilesPath;
    }
    public void setUpdateCoreFilesPath(File[] updateCoreFilesPath) {
        for(int i = 0; i< 4; i++){
            preferences.edit().putString(UPDATE_CORE_FILES + i, updateCoreFilesPath[i].getAbsolutePath()).commit();
        }
        this.updateCoreFilesPath = updateCoreFilesPath;
    }

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
        try {
            Logger.d(Logger.MAIN_LOG, "class for name: " + Class.forName("com.jcraft.jsch.jce.Random"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        updateOsFilePath = preferences.getString(UPDATE_OS_FILE_PATH, "");
        updateOsFileVersion = preferences.getString(UPDATE_OS_FILE_VERSION, "");
        showAccessPointDialog = preferences.getBoolean(IS_ASK_FOR_TENETH, true);
        updateCoreFilesPath = new File[4];
        for(int i = 0; i< 4; i++){
            updateCoreFilesPath[i] = new File(preferences.getString(UPDATE_CORE_FILES + i, ""));
        }

        if(!new File(updateOsFilePath).exists()){
            setUpdateOsFileVersion("");
            setUpdateOsFilePath("");
        }

        Downloader.tempUpdateOsFile = new File(updateOsFilePath);
        Downloader.setUpdateCoreFiles(updateCoreFilesPath);

        login = preferences.getString(LOGIN, "");
        password = preferences.getString(PASSWORD, "");
        isRemembered = preferences.getBoolean(IS_REMEMBERED, false);
        updateContentFilePaths = preferences.getStringSet(UPDATE_CONTENT_FILES, new HashSet<>());
    }

    public boolean isRemembered() {
        return isRemembered;
    }

    public String getPassword() {
        return password;
    }


    public void saveLoginInformation(String login, String password, boolean isRemembered){
        if(isRemembered){
            preferences.edit().putString(LOGIN, login).commit();
            preferences.edit().putString(PASSWORD, password).commit();
            preferences.edit().putBoolean(IS_REMEMBERED, true).commit();
        } else {
            resetLoginInformation();
        }
    }

    public void resetLoginInformation(){
        preferences.edit().putString(LOGIN, "").commit();
        preferences.edit().putString(PASSWORD, "").commit();
        preferences.edit().putBoolean(IS_REMEMBERED, false).commit();
    }

    public boolean isAskForTeneth(){
        return preferences.getBoolean(IS_ASK_FOR_TENETH, true);
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public void setAskForTeneth(boolean value) {
        preferences.edit().putBoolean(IS_ASK_FOR_TENETH, value).commit();
        this.showAccessPointDialog = value;
    }

    public String getUpdateOsFilePath() {
        return updateOsFilePath;
    }
    public void setUpdateOsFilePath(String updateOsFilePath) {
        preferences.edit().putString(UPDATE_OS_FILE_PATH, updateOsFilePath).commit();
        this.updateOsFilePath = updateOsFilePath;
    }

    public String getUpdateOsFileVersion() {
        return updateOsFileVersion;
    }
    public void setUpdateOsFileVersion(String updateOsFileVersion) {
        preferences.edit().putString(UPDATE_OS_FILE_VERSION, updateOsFileVersion).commit();
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

    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
}