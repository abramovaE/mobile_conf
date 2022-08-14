package com.kotofeya.mobileconfigurator.network.upload;

import java.io.File;
import java.net.URL;

public interface UploadCoreUpdateFileListener {
    void uploadFileSuccessful(File destinationFile, int index, String serial, String ip);
    void uploadFileFailed(File file, int index, String serial, String ip);
    void setProgress(int downloaded);
}
