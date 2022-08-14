package com.kotofeya.mobileconfigurator.network;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;

import java.io.File;
import java.util.Arrays;

public class DownloadFileUtils {

    public static File createTempUpdateFile(String fileName){
        File outputDir = App.get().getCacheDir();
        File file = new File(outputDir + "/" + fileName);
        if(file.exists()){
            file.delete();
        }
        file = new File(outputDir + "/" + fileName);
        file.deleteOnExit();
        return file;
    }


//
//    private void createUpdateOsFile(String fileName) {
//        File outputDir = App.get().getApplicationContext().getExternalFilesDir(null);
//        Logger.d(TAG, "tempUpdateOsFile: " + tempUpdateOsFile);
//        if(tempUpdateOsFile != null && tempUpdateOsFile.exists()){
//            Logger.d(TAG, "delete exist file");
//            tempUpdateOsFile.delete();
//        }
//        Logger.d(TAG, " creating new temp os file");
//        tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
//    }


}
