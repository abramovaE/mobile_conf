package com.kotofeya.mobileconfigurator.network.download;

import java.io.File;
import java.net.URL;

public interface DownloadFileListener {
    void downloadFileSuccessful(File destinationFile, int index);
    void downloadFileFailed(URL url, int index);
    void setProgress(int downloaded);
}
