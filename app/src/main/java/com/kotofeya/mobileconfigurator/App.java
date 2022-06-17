package com.kotofeya.mobileconfigurator;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.acra.ACRA;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.KEY_VALUE_LIST)
//@AcraHttpSender(uri = "http://95.161.210.44/mobile_conf_acra.php",
//        httpMethod = HttpSender.Method.POST)
public class App extends Application {

    private static final String TAG = Application.class.getSimpleName();

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
    private static final String SSID_ITERATION = "ssid_iteration";


    private String login;
    private String level;

    private SharedPreferences preferences;
    private String updateOsFilePath;
    private String updateOsFileVersion;

    private boolean showAccessPointDialog;
    private File[] updateCoreFilesPath;
    private Set<String> updateContentFilePaths;

    private String password;
    private boolean isRemembered;

    private Map<String, Integer> coreUpdateSsidIteration;


    public Map<String, Integer> getCoreUpdateSsidIteration() {
        return coreUpdateSsidIteration;
    }

    public void setCoreUpdateIterationsToPrefs(Map<String, Integer> map){
        Logger.d(TAG, "setCoreUpdateIterationsToPrefs(), map: " + map);
        Set<String> set = new HashSet<>();
        for (Map.Entry<String, Integer> pair: map.entrySet()){
            if(pair.getValue() > 0) {
                String value = pair.getKey() + "_" + pair.getValue();
                set.add(value);
            }
        }
        preferences.edit().putStringSet(SSID_ITERATION, set).commit();
        this.coreUpdateSsidIteration = map;
    }

    private Map<String, Integer> getCoreUpdateIterationsFromPref(){

        Map<String, Integer> map = new HashMap<>();
        Set<String> set = preferences.getStringSet(SSID_ITERATION, new HashSet<>());
        for(String s:  set){
            String[] ssidIteration = s.split("_");
            map.put(ssidIteration[0], Integer.parseInt(ssidIteration[1]));
        }
        Logger.d(TAG, "getCoreUpdateIterationsFromPrefs(), map: " + map);

        return map;
    }


    public Set<String> getUpdateContentFilePaths() {
        Logger.d(Logger.APP_LOG, "getUpdateContentFilePaths(): " + updateContentFilePaths);
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
        coreUpdateSsidIteration = getCoreUpdateIterationsFromPref();
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

    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }


    public void resetCoreFilesCounter(String serial){
        Logger.d(TAG, "resetCoreFilesCounter(), serial: " + serial);
        coreUpdateSsidIteration.put(serial, 0);
        setCoreUpdateIterationsToPrefs(coreUpdateSsidIteration);
    }

    public void putSsidIteration(String serial, int iteration){
        Logger.d(TAG, "putSsidIteration(), serial: " +
                serial + ", iteration: " + iteration);
        coreUpdateSsidIteration.put(serial, iteration);
        setCoreUpdateIterationsToPrefs(coreUpdateSsidIteration);
    }

}