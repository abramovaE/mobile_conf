package com.kotofeya.mobileconfigurator.domain.tempfiles;

import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.Map;

public interface TempFilesRepository {

    File[] getUpdateCoreFiles();
    void saveUpdateCoreFiles(File[] files);

    Map<String, Integer> getCoreUpdateIterationMap();
    void saveCoreUpdateIterationsMap(Map<String, Integer> map);
    void setIteration(String serial, int iteration);

    File getUpdateOsFile();
    void saveUpdateOsFile(File file);
    LiveData<String> getUpdateOsVersion();
    void setUpdateOsVersion(String version);

}
