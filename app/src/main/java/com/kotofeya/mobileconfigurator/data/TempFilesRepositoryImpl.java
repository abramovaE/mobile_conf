package com.kotofeya.mobileconfigurator.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.domain.tempfiles.TempFilesRepository;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TempFilesRepositoryImpl implements TempFilesRepository {

    private static final String TAG = TempFilesRepositoryImpl.class.getSimpleName();
    private static final String PREF_NAME = "mobile_conf_pref";
    private static final String UPDATE_CORE_FILES = "update_core_files_path";
    private static final String SSID_ITERATION = "ssid_iteration";

    private static final String UPDATE_OS_FILE_PATH = "update_os_file_path";
    private static final String UPDATE_OS_FILE_VERSION = "update_os_file_version";

    private SharedPreferences preferences = App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    private File[] updateCoreFilesPath = new File[4];

    private File updateOsFile;
    private String updateOsFileVersion;

    private Map<String, Integer> coreUpdateIterationMap = new HashMap<>();


    private static TempFilesRepositoryImpl instance;
    private TempFilesRepositoryImpl(){
        initUpdateCoreFilesPath();
        initCoreUpdateIterationMap();
        initUpdateOsFile();
    }
    public static TempFilesRepositoryImpl getInstance(){
        if(instance == null){
            instance = new TempFilesRepositoryImpl();
        }
        return instance;
    }

    private void initUpdateOsFile(){
        String updateOsFilePath = preferences.getString(UPDATE_OS_FILE_PATH, "");
        updateOsFileVersion = preferences.getString(UPDATE_OS_FILE_VERSION, "");
        if(!new File(updateOsFilePath).exists()){
            setUpdateOsVersion("");
        }
        updateOsFile = new File(updateOsFilePath);
    }

    @Override
    public File getUpdateOsFile() {
        return updateOsFile;
    }
    public void saveUpdateOsFile(File file) {
        preferences.edit().putString(UPDATE_OS_FILE_PATH, file.getAbsolutePath()).commit();
        this.updateOsFile = updateOsFile;
    }

    public String getUpdateOsVersion() {
        return updateOsFileVersion;
    }
    public void setUpdateOsVersion(String updateOsFileVersion) {
        preferences.edit().putString(UPDATE_OS_FILE_VERSION, updateOsFileVersion).commit();
        this.updateOsFileVersion = updateOsFileVersion;
    }

    private void initUpdateCoreFilesPath() {
        for(int i = 0; i < updateCoreFilesPath.length; i++){
            updateCoreFilesPath[i] = new File(preferences.getString(UPDATE_CORE_FILES + i, ""));
        }
    }
    @Override
    public File[] getUpdateCoreFiles() {
        return updateCoreFilesPath;
    }
    @Override
    public void saveUpdateCoreFiles(File[] files) {
        for(int i = 0; i < files.length; i++){
            preferences.edit().putString(UPDATE_CORE_FILES + i, files[i].getAbsolutePath()).commit();
        }
        this.updateCoreFilesPath = files;
    }

    private void initCoreUpdateIterationMap(){
        Set<String> set = preferences.getStringSet(SSID_ITERATION, new HashSet<>());
        for(String s: set){
            String[] ssidIteration = s.split("_");
            coreUpdateIterationMap.put(ssidIteration[0], Integer.parseInt(ssidIteration[1]));
        }
        Logger.d(TAG, "initCoreUpdateIterationMap(), map: " + coreUpdateIterationMap);
    }
    @Override
    public Map<String, Integer> getCoreUpdateIterationMap() {
        return coreUpdateIterationMap;
    }

    @Override
    public void saveCoreUpdateIterationsMap(Map<String, Integer> map) {
        Logger.d(TAG, "saveCoreUpdateIterationsMap(), map: " + map);
        Set<String> set = new HashSet<>();
        for (Map.Entry<String, Integer> pair: map.entrySet()){
            if(pair.getValue() > 0) {
                String value = pair.getKey() + "_" + pair.getValue();
                set.add(value);
            }
        }
        preferences.edit().putStringSet(SSID_ITERATION, set).commit();
        this.coreUpdateIterationMap = map;
    }

    @Override
    public void setIteration(String serial, int iteration) {
        Logger.d(TAG, "setIteration(), serial: " + serial + ", iteration: " + iteration);
        coreUpdateIterationMap.put(serial, iteration);
        saveCoreUpdateIterationsMap(coreUpdateIterationMap);
    }


}
