package com.kotofeya.mobileconfigurator.network;

import com.kotofeya.mobileconfigurator.App;

import java.io.File;

public class DownloadFileUtils {

    public static File createTempUpdateFile(String fileName){
        File outputDir = App.get().getCacheDir();
        File file = new File(outputDir + "/" + fileName);
        file.deleteOnExit();
        return file;
    }
}